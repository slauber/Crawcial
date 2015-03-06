package de.crawcial.database;

import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;
import org.lightcouch.DesignDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static int bufferLimit = 200;
    private static boolean downloadMedia = false;
    private ExecutorService es;
    private Vector<JsonObject> jsonObjectVector = new Vector<>(bufferLimit);
    private LinkedBlockingQueue<Runnable> attachmentExecutors;
    private WriteExecutor writeExecutor;
    private Thread writeExecutorThread;

    private int warningCnt = 0;

    private DatabaseService() {
    }

    public static DatabaseService getInstance() {
        return ourInstance;
    }

    public synchronized void increaseWarnings() {
        ++warningCnt;
    }

    public synchronized int getWarningCnt() {
        logger.warn("{} warnings reported", warningCnt);
        return warningCnt;
    }

    public boolean isDownloadMedia() {
        return downloadMedia;
    }

    public synchronized void init(boolean downloadMedia) {
        DatabaseService.downloadMedia = downloadMedia;
        warningCnt = 0;
        CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
        DesignDocument designDoc = dbClient.design().getFromDesk("crawcial");
        dbClient.design().synchronizeWithDb(designDoc);
        dbClient.shutdown();

        if (downloadMedia) {
            attachmentExecutors = new LinkedBlockingQueue<>();
            es = new ThreadPoolExecutor(4, 4, 30, TimeUnit.SECONDS, attachmentExecutors);
        }
        writeExecutor = new WriteExecutor(jsonObjectVector, bufferLimit);
        writeExecutorThread = new Thread(writeExecutor);
        writeExecutorThread.setName("writer-executor-thread-0");
        writeExecutorThread.start();
    }

    void loadAttachment(JsonObject status) {
        es.execute(new AttachmentExecutor(status, jsonObjectVector));
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
