package de.crawcial.database;

import com.google.gson.JsonObject;
import de.crawcial.database.util.CouchDbCloneClient;
import org.lightcouch.CouchDbClient;

import java.util.Vector;

/**
 * Created by Sebastian Lauber on 06.03.15.
 */
class WriteExecutor implements Runnable {
    private final Vector<JsonObject> vector;
    private final CouchDbClient dbClient = new CouchDbCloneClient(DatabaseService.getInstance().getDbProperties());
    private boolean alive = true;
    private int bufferLimit;

    public WriteExecutor(Vector<JsonObject> vector, int bufferLimit) {
        this.vector = vector;
        this.bufferLimit = bufferLimit;
    }

    @Override
    public void run() {
        while (vector.size() > 0 || alive) {
            if (vector.size() >= bufferLimit) {
                synchronized (vector) {
                    dbClient.bulk(vector, true);
                    vector.clear();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        alive = false;
        if (vector.size() > 0) {
            synchronized (vector) {
                dbClient.bulk(vector, true);
                vector.clear();
            }
        }
        dbClient.shutdown();
    }
}
