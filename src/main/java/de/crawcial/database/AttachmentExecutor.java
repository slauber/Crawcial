package de.crawcial.database;

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
import java.util.Base64;
import java.util.Vector;

/**
 * Created by Sebastian Lauber on 28.02.15.
 */
class AttachmentExecutor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentExecutor.class);
    private final JsonObject json;
    private final Vector<JsonObject> target;

    public AttachmentExecutor(JsonObject json, Vector<JsonObject> target) {
        this.json = json;
        this.target = target;
    }

    @Override
    public void run() {
        // Get media urls from json
        JsonArray mediaArray = json.get("extended_entities").getAsJsonObject()
                .get("media").getAsJsonArray();


        JsonObject attachmentObjRoot = new JsonObject();

        // Iterate through media array (if more than zero exist)
        for (int i = 0; i < mediaArray.size(); ++i) {
            JsonObject attachmentObj = new JsonObject();
            String urlString = mediaArray.get(i).getAsJsonObject().get("media_url_https").getAsString() + ":small";
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


