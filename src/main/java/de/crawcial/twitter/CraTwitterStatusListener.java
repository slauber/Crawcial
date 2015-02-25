package de.crawcial.twitter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.twitter.hbc.twitter4j.handler.StatusStreamHandler;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.message.StallWarningMessage;
import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.MediaEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sebastian Lauber on 24.02.15.
 */
public class CraTwitterStatusListener implements StatusStreamHandler {
    final static private Logger logger = LoggerFactory.getLogger(CraTwitterStatusListener.class);
    final CouchDbClient client;

    public CraTwitterStatusListener(CouchDbClient client) {
        this.client = client;
    }

    private void persistStatus(Status status, boolean downloadMedia) {
        // Initialize JsonObject -> CouchDB document
        JsonObject js = new JsonObject();

        // Initialize JsonArray -> Media
        JsonArray jsMe = new JsonArray();

        // Load MediaEntities from Status & initialize HashMap for attachments
        List<MediaEntity> me = Arrays.asList(status.getMediaEntities());
        HashMap<String[], byte[]> meAttach = new HashMap<>();

        // Iterate through MediaEntities and download them to ByteArrays
        for (MediaEntity m : me) {
            String[] metaContent = new String[2];
            JsonObject meJsO = new JsonObject();
            meJsO.addProperty("type", m.getType());
            meJsO.addProperty("url", m.getMediaURLHttps());
            jsMe.add(meJsO);

            // Check for download media flag and skip if not set
            if (downloadMedia) {
                URL url;
                try {
                    logger.debug("Downloading from URL: {}", m.getMediaURLHttps());
                    url = new URL(m.getMediaURLHttps());
                    String contentType = url.openConnection().getContentType();
                    InputStream in = new BufferedInputStream(url.openStream());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n = 0;
                    while (-1 != (n = in.read(buf))) {
                        out.write(buf, 0, n);
                    }
                    out.close();
                    in.close();
                    byte[] response = out.toByteArray();
                    metaContent[0] = m.getMediaURLHttps();
                    metaContent[1] = contentType;
                    meAttach.put(metaContent, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // Add further content to JsonObject
        js.addProperty("screenname", status.getUser().getScreenName());
        js.addProperty("message", status.getText());
        js.add("media", jsMe);

        // Persist tweet
        Response r = client.save(js);

        // Add media attachments
        if (downloadMedia) {
            for (Map.Entry<String[], byte[]> entry : meAttach.entrySet()) {
                client.saveAttachment(new ByteArrayInputStream(entry.getValue()), entry.getKey()[0],
                        entry.getKey()[1], r.getId(), r.getRev());
                logger.debug("Persisted media: {}, MIME: {}", entry.getKey()[0], entry.getKey()[1]);
            }
        }
    }

    @Override
    public void onStatus(Status status) {
        persistStatus(status, true);
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    @Override
    public void onTrackLimitationNotice(int i) {

    }

    @Override
    public void onScrubGeo(long l, long l1) {

    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {

    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void onDisconnectMessage(DisconnectMessage disconnectMessage) {

    }

    @Override
    public void onStallWarningMessage(StallWarningMessage stallWarningMessage) {

    }

    @Override
    public void onUnknownMessageType(String s) {

    }
}
