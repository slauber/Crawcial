package de.crawcial.database;

import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Lauber on 28.02.15.
 */
class DatabaseWriter implements Runnable {

    private final ArrayList<JsonObject> objects;
    private final CouchDbClient dbClient = new CouchDbClient("couchdb.properties");

    public DatabaseWriter(ArrayList<JsonObject> objects) {
        this.objects = objects;
    }


    @Override
    public void run() {
        List<Response> responses = dbClient.bulk(objects, true);
        if (DatabaseService.getInstance().getDownloadMedia()) {
            AttachmentDispatcher ad = AttachmentDispatcher.getInstance();
            for (int i = 0; i < objects.size(); ++i) {
                JsonObject object = objects.get(i);
                if (object.has("media")) {
                    Response response = responses.get(i);
                    ad.addDownloader(
                            new DatabaseAttachment(response.getId(), response.getRev(), object));
                }
            }
        }
        dbClient.shutdown();
    }
}
