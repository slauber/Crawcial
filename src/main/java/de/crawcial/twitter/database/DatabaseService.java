package de.crawcial.twitter.database;

import com.google.gson.JsonObject;
import de.crawcial.twitter.TwitterStreamer;
import de.crawcial.util.CouchDbCloneClient;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
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
    LinkedBlockingQueue<Runnable> attachmentExecutors;
    private ExecutorService es;
    private Vector<JsonObject> jsonObjectVector = new Vector<>(bufferLimit);
    private WriteExecutor writeExecutor;
    private Thread writeExecutorThread;
    private CouchDbProperties dbProperties;
    private int warningCnt = 0;
    private String imgSize = "small";
    private boolean mediaHttps = true;

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

    public synchronized void init(boolean downloadMedia, CouchDbProperties dbProperties, String imgSize, boolean mediaHttps) {
        DatabaseService.downloadMedia = downloadMedia;
        setMediaHttps(mediaHttps);
        setImgSize(imgSize);
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
        Runtime r = Runtime.getRuntime();
        if (r.totalMemory() * 1.1 >= r.maxMemory() && r.totalMemory() / (float) r.freeMemory() > 5) {
            System.gc();
            TwitterStreamer.getInstance().setLowMemory(true);
            downloadMedia = false;
            jsonObjectVector.add(status);
            logger.warn("Persistence disable due to mem limit");
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
            if (es.awaitTermination(30, TimeUnit.SECONDS)) {
                es.shutdownNow();
            }
        }
        writeExecutor.shutdown();
        writeExecutorThread.join();
    }

    public synchronized void forceShutdown() {
        jsonObjectVector.clear();
        attachmentExecutors.clear();
        es.shutdownNow();
    }

    public String getImgSize() {
        return imgSize;
    }

    public void setImgSize(String imgSize) {
        this.imgSize = imgSize;
    }

    public boolean isMediaHttps() {
        return mediaHttps;
    }

    public void setMediaHttps(boolean mediaHttps) {
        this.mediaHttps = mediaHttps;
    }
}
