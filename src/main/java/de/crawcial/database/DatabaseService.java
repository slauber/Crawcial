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
import java.util.List;

/**
 * Created by Sebastian Lauber on 27.02.15.
 */
public class DatabaseService {
    private static final DatabaseService ourInstance = new DatabaseService();
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static final int bufferLimit = 100;
    private static boolean downloadMedia = false;

    private static ArrayList<JsonObject> dbQueue = new ArrayList<>(bufferLimit);
    private static int cnt;

    private AttachmentDispatcher ad = AttachmentDispatcher.getInstance();

    private DatabaseService() {
    }

    public static DatabaseService getInstance() {
        return ourInstance;
    }

    public void init(boolean downloadMedia) {
        DatabaseService.downloadMedia = downloadMedia;
        if (downloadMedia) {
            ad.init();
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
        flushQueue();
        AttachmentDispatcher.getInstance().shutdown();
        if (AttachmentDispatcher.getInstance().running) {
            AttachmentDispatcher.getInstance().join();
        }
    }

    private void flushQueue() {
        // Replaces the dbQueue with an empty one and run the DatabaseWriter
        ArrayList<JsonObject> dbQueueClone = (ArrayList<JsonObject>) dbQueue.clone();
        dbQueue = new ArrayList<>(bufferLimit);
        new Thread(new DatabaseWriter(dbQueueClone)).start();
    }

    public void persist(Status twitterStatus) {
        // Prepares the Status object and queues it for persisting
        dbQueue.add(prepareForQueue(twitterStatus));

        // flushQueue triggered if bufferLimit exceeded
        if (dbQueue.size() >= bufferLimit) {
            flushQueue();
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
