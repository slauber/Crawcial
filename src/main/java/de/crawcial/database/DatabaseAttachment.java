package de.crawcial.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;
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
    private String rev;

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

            String urlString = mediaArray.get(i).getAsJsonObject().get("url").getAsString() + ":small";
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
                    dbClient.saveAttachment(new ByteArrayInputStream(responseBytes), urlString, contentType,
                            id, rev);
                } else {
                    // Flag media as unavailable
                    mediaArray.get(i).getAsJsonObject().addProperty("unavailable", true);
                    downloadError = true;
                }

            } catch (MalformedURLException e) {
                logger.error("Malformed URL while downloading attachment - {}", urlString);
            } catch (IOException e) {
                logger.error("IOException during attachment download - {}", e.getLocalizedMessage());
            }
            if (downloadError) {
                json.addProperty("_rev", rev);
                json.add("media", mediaArray);
                rev = dbClient.update(json).getRev();
            }
        }
        dbClient.shutdown();
    }
}

