package de.crawcial.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            String urlString = mediaArray.get(i).getAsJsonObject().get("media_url").getAsString() + ":small";
            URL url;

            // Download into byte[]
            try {
                url = new URL(urlString);
                logger.debug("Downloading: {}", urlString);
                String contentType = url.openConnection().getContentType();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                StringBuilder sb = new StringBuilder();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
                in.close();

                // Check, whether download was successful
                if (contentType != null) {
                    // Store as attachment
                    String attachmentString = Base64.getEncoder().encodeToString(sb.toString().getBytes());
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
            logger.debug("Download ok - id: {}", json.get("_id"));
        }
        target.add(json);
    }
}


