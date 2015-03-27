package de.crawcial.twitter.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Vector;

/**
 * Objects of this class are used to download attachments from the Twitter CDN and queue them on the database vector.
 *
 * @author Sebastian Lauber
 */
class AttachmentExecutor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentExecutor.class);
    private static DatabaseService dbService;
    private final JsonObject json;
    private final Vector<JsonObject> target;

    /**
     * This constructor is used to set the Tweet JSON and the target vector.
     *
     * @param json   Tweet JSON
     * @param target database target vector
     */
    public AttachmentExecutor(JsonObject json, Vector<JsonObject> target) {
        this.json = json;
        this.target = target;
        dbService = DatabaseService.getInstance();
    }

    @Override
    public void run() {
        // Get media urls from json
        JsonArray mediaArray = json.get("extended_entities").getAsJsonObject().get("media").getAsJsonArray();
        JsonObject attachmentObjRoot = new JsonObject();

        // Iterate through media array (if more than zero exist)
        for (int i = 0; i < mediaArray.size(); ++i) {
            JsonObject attachmentObj = new JsonObject();
            String urlString = mediaArray.get(i).getAsJsonObject().get(dbService.isMediaHttps() ?
                    "media_url_https" : "media_url").getAsString() + ":" + dbService.getImgSize();
            URL url;

            // Download into byte[]
            try {
                url = new URL(urlString);

                logger.debug("Downloading: {}", urlString);

                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(2500);
                urlConnection.setReadTimeout(2500);
                String contentType = urlConnection.getContentType();

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
                    String attachmentString = Base64.getEncoder().encodeToString(responseBytes);
                    attachmentObj.addProperty("content_type", contentType);
                    attachmentObj.addProperty("data", attachmentString);
                    attachmentObjRoot.add(urlString, attachmentObj);
                }

            } catch (MalformedURLException e) {
                logger.error("Malformed URL while downloading attachment - {}", urlString);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        if (!attachmentObjRoot.isJsonNull()) {
            json.add("_attachments", attachmentObjRoot);
        }

        target.add(json);

    }
}


