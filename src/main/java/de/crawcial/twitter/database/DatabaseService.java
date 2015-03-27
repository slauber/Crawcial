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
 * This singleton class handles all database activities of Crawcial for Twitter.
 *
 * @author Sebastian Lauber
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

    /**
     * Returns the Crawcial for Twitter DatabaseService singleton.
     *
     * @return Crawcial for Twitter DatabaseService singleton
     */
    public static DatabaseService getInstance() {
        return ourInstance;
    }

    /**
     * Returns the current database connection properties.
     *
     * @return current CouchDB connection properties
     */
    public CouchDbProperties getDbProperties() {
        return dbProperties;
    }

    /**
     * This method must be called, if a message from Twitter does not contain a valid Tweet.
     */
    public synchronized void increaseWarnings() {
        ++warningCnt;
    }

    /**
     * Returns the warning count, that indicates the number of warning messages received from Twitter.
     *
     * @return warning count
     */
    public synchronized int getWarningCnt() {
        return warningCnt;
    }

    /**
     * Returns true, if the media downloader is enabled.
     *
     * @return true, if the media downloader is enabled
     */
    public boolean isDownloadMedia() {
        return downloadMedia;
    }

    /**
     * Configures the DatabaseService singleton, must be called before the crawling process starts.
     *
     * @param downloadMedia true, if media downloader enabled, false to disable media downloads
     * @param dbProperties  CouchDB properties for the Crawcial Twitter Database
     * @param imgSize       requested image size (thumb, small, medium, large)
     * @param mediaHttps    true if https should be used for media downloads
     */
    public synchronized void init(boolean downloadMedia, CouchDbProperties dbProperties, String imgSize, boolean mediaHttps) {
        DatabaseService.downloadMedia = downloadMedia;
        this.mediaHttps = mediaHttps;
        this.imgSize = imgSize;
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

    /**
     * Triggers an attachment download of the status.
     *
     * @param status Tweet for attachment download
     */
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

    /**
     * Returns the current outgoing vector to the database.
     *
     * @return outgoing JSON vector
     */
    public Vector<JsonObject> getJsonObjectVector() {
        return jsonObjectVector;
    }

    /**
     * Initiates a proper shutdown.
     *
     * @throws InterruptedException if interrupted
     */
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

    /**
     * Forces a shutdown (clears queues and shuts down all workers).
     */
    public synchronized void forceShutdown() {
        jsonObjectVector.clear();
        attachmentExecutors.clear();
        es.shutdownNow();
    }

    /**
     * Returns the configured image download size.
     *
     * @return current configured image download size (thumb, small, medium, large)
     */
    public String getImgSize() {
        return imgSize;
    }

    /**
     * Returns true, if https should be used for media downloads.
     *
     * @return true, if https should be used for media downloads
     */
    public boolean isMediaHttps() {
        return mediaHttps;
    }
}
