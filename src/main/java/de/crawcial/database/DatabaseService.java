package de.crawcial.database;

import com.google.gson.JsonObject;
import de.crawcial.database.util.CouchDbCloneClient;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import org.lightcouch.DesignDocument;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sebastian Lauber on 27.02.15.
 */
public class DatabaseService {
    private static final DatabaseService ourInstance = new DatabaseService();
    private static int bufferLimit = 200;
    private static boolean downloadMedia = false;
    LinkedBlockingQueue<Runnable> attachmentExecutors;
    private ExecutorService es;
    private Vector<JsonObject> jsonObjectVector = new Vector<>(bufferLimit);
    private WriteExecutor writeExecutor;
    private Thread writeExecutorThread;
    private CouchDbProperties dbProperties;
    private int warningCnt = 0;

    private DatabaseService() {
    }

    public static DatabaseService getInstance() {
        return ourInstance;
    }

    public CouchDbProperties getDbProperties() {
        return dbProperties;
    }

    public synchronized void increaseWarnings() {
        ++warningCnt;
    }

    public synchronized int getWarningCnt() {
        return warningCnt;
    }

    public boolean isDownloadMedia() {
        return downloadMedia;
    }

    public synchronized void init(boolean downloadMedia, CouchDbProperties dbProperties) {
        DatabaseService.downloadMedia = downloadMedia;
        warningCnt = 0;
        this.dbProperties = dbProperties;
        CouchDbClient dbClient = new CouchDbCloneClient(dbProperties);
        DesignDocument designDoc = dbClient.design().getFromDesk("crawcial");
        dbClient.design().synchronizeWithDb(designDoc);
        dbClient.shutdown();

        attachmentExecutors = new LinkedBlockingQueue<>();
        if (downloadMedia) {
            es = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().availableProcessors() * 2, 30, TimeUnit.SECONDS, attachmentExecutors);
        }
        writeExecutor = new WriteExecutor(jsonObjectVector, bufferLimit);
        writeExecutorThread = new Thread(writeExecutor);
        writeExecutorThread.setName("writer-executor-thread-0");
        writeExecutorThread.start();
    }

    void loadAttachment(JsonObject status) {
        if (Runtime.getRuntime().totalMemory() / (float) Runtime.getRuntime().freeMemory() < 0.125) {
            downloadMedia = false;
            jsonObjectVector.add(status);
        } else {
            es.execute(new AttachmentExecutor(status, jsonObjectVector));
        }
    }

    public Vector<JsonObject> getJsonObjectVector() {
        return jsonObjectVector;
    }

    public void shutdown() throws InterruptedException {
        if (es != null) {
            es.shutdown();
            if (es.awaitTermination(1, TimeUnit.MINUTES)) {
                es.shutdownNow();
            }
        }
        writeExecutor.shutdown();
        writeExecutorThread.join();
    }
}
