package de.crawcial.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;
import org.lightcouch.DocumentConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Sebastian Lauber on 28.02.15.
 */
class DatabaseAttachment implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAttachment.class);
    private final String id;
    private final JsonObject json;
    final private int maxRetry = 10;
    private String rev;
    private int retryCnt = 0;

    // Gets initialized with doc id, rev and json
    public DatabaseAttachment(String id, String rev, JsonObject json) {
        this.json = json;
        this.id = id;
        this.rev = rev;
    }


    @Override
    public void run() {
        // Get media urls from json
        JsonArray mediaArray = json.get("media").getAsJsonArray();

        // If an error occurs, persist error state
        boolean downloadError = false;

        // Open database connection
        CouchDbClient dbClient = new CouchDbClient("couchdb.properties");

        // Iterate through media array (if more than zero exist)
        for (int i = 0; i < mediaArray.size(); ++i) {
            String urlString;
            // If 5 times retried, try to get without size params
            if (retryCnt > 5) {
                urlString = mediaArray.get(i).getAsJsonObject().get("url").getAsString();
            } else {
                urlString = mediaArray.get(i).getAsJsonObject().get("url").getAsString() + ":small";
            }
            URL url;

            // Download into byte[]
            try {
                url = new URL(urlString);

                logger.debug("Downloading: {}", urlString);

                String contentType = url.openConnection().getContentType();
                InputStream in = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] responseBytes = out.toByteArray();

                // Check, whether download was successful
                if (contentType != null) {
                    // Store as attachment
                    rev = dbClient.saveAttachment(new ByteArrayInputStream(responseBytes), urlString, contentType,
                            id, rev).getRev();
                } else {
                    // Flag media as unavailable
                    mediaArray.get(i).getAsJsonObject().addProperty("unavailable", true);
                    downloadError = true;
                }

            } catch (MalformedURLException e) {
                logger.error("Malformed URL while downloading attachment - {}", urlString);
                // Flag media as unavailable
                mediaArray.get(i).getAsJsonObject().addProperty("unavailable", true);
                downloadError = true;
            } catch (IOException e) {
                // Put back to queue, if not exceeded maxRetried
                if (!isMaxRetried()) {
                    increaseRetryCnt();
                    logger.error("IOException during attachment download - {}", e.getLocalizedMessage());
                    logger.error("retrying... - {}", e.getLocalizedMessage());
                    AttachmentDispatcher.getInstance().addDownloader(this);
                } else {
                    // Else flag media as unavailable
                    logger.error("IOException during attachment download - {}", e.getLocalizedMessage());
                    mediaArray.get(i).getAsJsonObject().addProperty("unavailable", true);
                    downloadError = true;
                }
            } catch (DocumentConflictException e) {
                logger.error("Conflict - ID: {} rev: {}", id, rev);
                e.printStackTrace();
            }
            if (downloadError) {
                logger.error("Flagging as download error: {} - URL: {}", id, urlString);
                json.addProperty("_rev", rev);
                json.add("media", mediaArray);
                rev = dbClient.update(json).getRev();
            }
        }
        AttachmentDispatcher.getInstance().downloadDone(this);
        dbClient.shutdown();
    }

    public void increaseRetryCnt() {
        ++retryCnt;
    }

    public boolean isMaxRetried() {
        return (retryCnt >= maxRetry);
    }
}

