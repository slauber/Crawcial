package de.crawcial.twitter.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Waits for new Tweets on the queue and schedules a media download (if necessary) or puts it to the outgoing vector.
 *
 * @author Sebastian Lauber
 */
public class LoadExecutor implements Runnable {

    final static private Logger logger = LoggerFactory.getLogger(LoadExecutor.class);
    private final BlockingQueue<String> queue;
    private final Vector<JsonObject> target;
    private boolean running = true;
    private DatabaseService ds = DatabaseService.getInstance();

    /**
     * This constructor sets the incoming queue and the outgoing vector.
     *
     * @param queue  incoming queue of Tweets
     * @param target outgoing vector of JSON
     */
    public LoadExecutor(BlockingQueue<String> queue, Vector<JsonObject> target) {
        this.queue = queue;
        this.target = target;
    }

    @Override
    public void run() {
        while (running) {
            String jsonString = null;
            try {
                jsonString = queue.poll(50, TimeUnit.MILLISECONDS);
                if (jsonString != null) {
                    JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
                    if (json.has("text")) {
                        json.remove("id");
                        json.addProperty("_id", json.get("id_str").getAsString());
                        json.remove("id_str");
                        if (ds.isDownloadMedia()
                                && json.has("extended_entities")
                                && json.get("extended_entities").getAsJsonObject().has("media")) {
                            DatabaseService.getInstance().loadAttachment(json);
                        } else {
                            target.add(json);
                        }
                    } else {
                        logger.warn("Not a status: {}", jsonString);
                        ds.increaseWarnings();
                    }
                }
            } catch (InterruptedException | NullPointerException e) {
                e.printStackTrace();
            } catch (JsonSyntaxException e) {
                logger.error("JSON parsing error: {}\n{}", jsonString, e.getLocalizedMessage());
                ds.increaseWarnings();
            }
        }
    }

    /**
     * Initiates a proper shutdown.
     */
    public void shutdown() {
        running = false;
    }
}
