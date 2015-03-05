package de.crawcial.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;
import org.lightcouch.DesignDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Sebastian Lauber on 27.02.15.
 */
public class DatabaseService {
    private static final DatabaseService ourInstance = new DatabaseService();
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static final int bufferLimit = 100;
    private static boolean downloadMedia = false;

    private static HashSet<Long> hs1 = new HashSet<>(10000);
    private static HashSet<Long> hs2 = new HashSet<>(10000);
    private static boolean useHs1 = true;

    private static ArrayList<JsonObject> dbQueue = new ArrayList<>(bufferLimit);
    private static int cnt;

    private DatabaseAttachDispatcher dad;

    private DatabaseService() {
    }

    public static DatabaseService getInstance() {
        return ourInstance;
    }

    public void init(boolean downloadMedia) {
        DatabaseService.downloadMedia = downloadMedia;
        if (downloadMedia) {
            DatabaseAttachDispatcher.getInstance().reset();
            dad = DatabaseAttachDispatcher.getInstance();
            dad.start();
            logger.debug("AttachmentDispatcher started - State: {}", dad.getState());
        }
        cnt = 0;
        CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
        DesignDocument designDoc = dbClient.design().getFromDesk("crawcial");
        dbClient.design().synchronizeWithDb(designDoc);
        dbClient.shutdown();
    }

    public void increaseCnt() {
        ++cnt;
    }

    public int getCnt() {
        return cnt;
    }

    boolean getDownloadMedia() {
        return downloadMedia;
    }

    public void shutdown() throws InterruptedException {
        // On shutdown, flush the queue
        logger.debug("DatabaseService shutdown");
        flushQueue(true);
        if (dad != null) {
            logger.debug("AttachmentDispatcher shutdown initiated");
            dad.shutdown();
            logger.debug("AttachmentDispatcher shutdown received - State: {}", dad.getState());
            dad.join();
            logger.debug("AttachmentDispatcher shutdown completed - State: {}", dad.getState());
        }
    }

    private void flushQueue(boolean block) throws InterruptedException {
        // Replaces the dbQueue with an empty one and run the DatabaseWriter
        ArrayList<JsonObject> dbQueueClone = (ArrayList<JsonObject>) dbQueue.clone();
        dbQueue = new ArrayList<>(bufferLimit);
        Thread t = new Thread(new DatabaseWriter(dbQueueClone));
        t.start();
        if (block) {
            t.join();
        }
    }

    public void persist(Status twitterStatus) {
        if (!isDuplicate(twitterStatus.getId())) {
            // Prepares the Status object and queues it for persisting
            dbQueue.add(prepareForQueue(twitterStatus));

            // flushQueue triggered if bufferLimit exceeded
            if (dbQueue.size() >= bufferLimit) {
                try {
                    flushQueue(false);
                } catch (InterruptedException e) {
                    logger.error("Error while persisting: {}", e.getLocalizedMessage());
                }
            }
        }
    }

    boolean isDuplicate(long statusId) {
        if (useHs1 && hs1.size() >= 10000) {
            useHs1 = false;
            hs2 = new HashSet<>(10000);
        }
        if (!useHs1 && hs2.size() >= 10000) {
            useHs1 = true;
            hs1 = new HashSet<>(10000);
        }

        if (hs1.contains(statusId) || hs2.contains(statusId)) {
            logger.warn("Duplicate detected: {}", statusId);
            --cnt;
            return true;
        } else {
            if (useHs1) {
                hs1.add(statusId);
            } else {
                hs2.add(statusId);
            }
            return false;
        }
    }

    JsonObject prepareForQueue(Status status) {
        // Initialize JsonObject -> CouchDB document
        JsonObject js = new JsonObject();

        // Add media urls
        List<MediaEntity> media = Arrays.asList(status.getMediaEntities());
        if (media.size() > 0) {
            JsonArray jsMedia = new JsonArray();
            for (MediaEntity mediaEntity : media) {
                JsonObject jsMediaO = new JsonObject();
                jsMediaO.addProperty("type", mediaEntity.getType());
                jsMediaO.addProperty("url", mediaEntity.getMediaURLHttps());
                jsMedia.add(jsMediaO);
            }
            js.add("media", jsMedia);
        }

        // Add links
        List<URLEntity> urls = Arrays.asList(status.getURLEntities());
        if (urls.size() > 0) {
            JsonArray jsLinks = new JsonArray();
            for (URLEntity url : urls) {
                JsonObject jsLinksO = new JsonObject();
                jsLinksO.addProperty("short_url", url.getText());
                jsLinksO.addProperty("long_url", url.getExpandedURL());
                jsLinks.add(jsLinksO);
            }
            js.add("links", jsLinks);
        }

        // Add further content to JsonObject
        js.addProperty("screenname", status.getUser().getScreenName());
        js.addProperty("message", status.getText());
        js.addProperty("_id", String.valueOf(status.getId()));
        return js;
    }
}
