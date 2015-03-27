package de.crawcial.twitter.database;

import com.google.gson.JsonObject;
import de.crawcial.util.CouchDbCloneClient;
import org.lightcouch.CouchDbClient;

import java.util.Vector;

/**
 * This of this runnable flush the given JSONObject vector to the database.
 *
 * @author Sebastian Lauber
 */
class WriteExecutor implements Runnable {
    private final Vector<JsonObject> vector;
    private CouchDbClient dbClient;
    private boolean alive = true;
    private int bufferLimit;

    /**
     * This constructor sets the outgoing vector and the maximal size of that buffer.
     *
     * @param vector      outgoing vector of JSONObjects (containing Tweets)
     * @param bufferLimit maximal size of the outgoing vector
     */
    public WriteExecutor(Vector<JsonObject> vector, int bufferLimit) {
        this.vector = vector;
        this.bufferLimit = bufferLimit;
    }

    @Override
    public void run() {
        while (vector.size() > 0 || alive) {
            if (vector.size() >= bufferLimit) {
                synchronized (vector) {
                    dbClient = new CouchDbCloneClient(DatabaseService.getInstance().getDbProperties());
                    dbClient.bulk(vector, true);
                    vector.clear();
                    dbClient.shutdown();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initiates a proper shutdown.
     */
    public void shutdown() {
        dbClient = new CouchDbCloneClient(DatabaseService.getInstance().getDbProperties());
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
