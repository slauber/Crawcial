package de.crawcial.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Sebastian Lauber on 28.02.15.
 */
class DatabaseAttachment implements Runnable {
    private final String id;
    private final String rev;
    private final JsonObject json;

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

        // Open database connection
        CouchDbClient dbClient = new CouchDbClient("couchdb.properties");

        // Iterate through media array (if more than zero exist)
        for (int i = 0; i < mediaArray.size(); ++i) {
            String urlString = mediaArray.get(i).getAsJsonObject().get("url").getAsString() + ":small";
            URL url;

            // Download into byte[]
            try {
                url = new URL(urlString);
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

                // Store as attachment
                dbClient.saveAttachment(new ByteArrayInputStream(responseBytes), urlString, contentType,
                        id, rev);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dbClient.shutdown();
    }
}

