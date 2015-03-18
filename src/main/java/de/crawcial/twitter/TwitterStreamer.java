package de.crawcial.twitter;

/**
 * Created by Sebastian Lauber on 21.02.15.
 */

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import de.crawcial.twitter.database.DatabaseService;
import de.crawcial.twitter.database.LoadExecutor;
import org.lightcouch.CouchDbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TwitterStreamer implements Runnable {

    final static private Logger logger = LoggerFactory.getLogger(TwitterStreamer.class);
    private final static String[] imgSizes = {"thumb", "small", "medium", "large"};
    private static TwitterStreamer ourInstance = new TwitterStreamer();
    private final DatabaseService ds = DatabaseService.getInstance();
    private Authentication auth;
    private List<String> terms;
    private List<Location> locations;
    private boolean configSet = false;
    private boolean active = false;
    private boolean running = false;
    private boolean lowMemory = false;
    private long result;
    private int threads = Runtime.getRuntime().availableProcessors();
    private Date startDate;

    public static TwitterStreamer getInstance() {
        return ourInstance;
    }

    public boolean isLowMemory() {
        return lowMemory;
    }

    public void setLowMemory(boolean lowMemory) {
        this.lowMemory = lowMemory;
    }

    public boolean isActive() {
        return active;
    }

    public List<String> getTerms() {
        return terms;
    }

    public boolean isRunning() {
        return running;
    }

    public Date getStartDate() {
        return startDate;
    }

    public long getResult() {
        return result;
    }

    public void setConfig(Authentication auth, List<String> terms, boolean downloadMedia,
                          CouchDbProperties properties, String imgSize, boolean mediaHttps, Location l) throws IllegalArgumentException {
        if (!downloadMedia ^ Arrays.asList(imgSizes).contains(imgSize)) {
            // Receive OAuth params
            this.auth = auth;

            if (l != null) {
                locations = Arrays.asList(l);
            } else {
                locations = null;
            }

            // Reset DatabaseService & set download mode
            ds.init(downloadMedia, properties, imgSize, mediaHttps);

            // Terms for filtering
            this.terms = terms;
            logger.info("Filtering tweets with terms {}", terms.toString());
            lowMemory = false;
            configSet = true;
        } else {
            configSet = false;
            throw new IllegalArgumentException("Invalid imgSize");
        }
    }

    public void shutdown() {
        active = false;
    }

    public void forceShutdown() {
        DatabaseService.getInstance().forceShutdown();
    }

    @Override
    public void run() {
        running = true;
        active = true;
        startDate = new Date(System.currentTimeMillis());
        if (configSet) {
            result = 0;
            // Create an appropriately sized blocking queue
            BlockingQueue<String> queue = new LinkedBlockingQueue<>(1000);

            // Create a filtered endpoint
            StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();

            // Add tracked terms
            if (terms != null) {
                endpoint.trackTerms(terms);
            }

            // Add tracked locations
            if (locations != null) {
                endpoint.locations(locations);
            }

            // Create a new BasicClient. By default gzip is enabled.
            BasicClient client = new ClientBuilder()
                    .hosts(Constants.STREAM_HOST)
                    .endpoint(endpoint)
                    .authentication(auth)
                    .processor(new StringDelimitedProcessor(queue))
                    .build();

            // Start loadExecutors
            LoadExecutor[] loadExecutors = new LoadExecutor[threads];
            ArrayList<Thread> loadExecutorThreads = new ArrayList<>();

            for (int i = 0; i < threads; ++i) {
                loadExecutors[i] = new LoadExecutor(queue, ds.getJsonObjectVector());
                loadExecutorThreads.add(new Thread(loadExecutors[i]));
            }

            Iterator<Thread> loadExecutorThreadsIt = loadExecutorThreads.iterator();
            int cnt = 0;
            while (loadExecutorThreadsIt.hasNext()) {
                Thread t = loadExecutorThreadsIt.next();
                t.setName("load-executor-" + cnt++);
                t.start();
            }

            // Connect the hosebird client
            client.connect();

            try {
                // Keeping this tool alive as set up in the properties file
                while (active) {
                    Thread.sleep(50);
                    result = client.getStatsTracker().getNumMessages() - DatabaseService.getInstance().getWarningCnt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Disconnect the hosebird client
            client.stop();

            System.gc();

            // Shutdown loadExecutors gracefully and join their threads
            for (LoadExecutor l : loadExecutors) {
                l.shutdown();
            }

            loadExecutorThreadsIt = loadExecutorThreads.iterator();
            while (loadExecutorThreadsIt.hasNext()) {
                try {
                    loadExecutorThreadsIt.next().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Shutdown the database service (AttachmentExecutors / WriteExecutor)
            try {
                ds.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.gc();

            // Return the num of messages - warnings (that are not persisted)
            result = client.getStatsTracker().getNumMessages() - DatabaseService.getInstance().getWarningCnt();
        } else {
            throw new IllegalStateException("Config not set");
        }
        running = false;
        configSet = false;
    }
}

