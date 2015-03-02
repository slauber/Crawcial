package de.crawcial.database;

import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;
import org.lightcouch.NoDocumentException;
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

    void checkAttachments() {
        try {
            List<JsonObject> noDownloads = dbClient.view("crawcial/noDownloads").includeDocs(true).query(JsonObject.class);
            for (JsonObject element : noDownloads) {
                String id = element.get("_id").getAsString();
                String rev = element.get("_rev").getAsString();
                new Thread(new DatabaseAttachment(id, rev, element));
            }
        } catch (NoDocumentException e) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.out.println("No document");
        }
    }

    @Override
    public void run() {
        if (DatabaseService.getInstance().getDownloadMedia()) {
            checkAttachments();
        }
        List<Response> responses = dbClient.bulk(objects, true);
        if (DatabaseService.getInstance().getDownloadMedia()) {
            for (int i = 0; i < objects.size(); ++i) {
                JsonObject object = objects.get(i);
                if (object.has("media")) {
                    Response response = responses.get(i);
                    try {
                        Thread.sleep((int) (15 * Math.random()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new Thread(new DatabaseAttachment(response.getId(), response.getRev(), object)).start();
                }
            }
        }
        dbClient.shutdown();
    }
}
