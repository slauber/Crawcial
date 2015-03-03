package de.crawcial.twitter;

/**
 * Created by Sebastian Lauber on 21.02.15.
 */

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.StatsReporter;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.twitter4j.Twitter4jStatusClient;
import de.crawcial.database.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.StatusListener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

class CraTwitterStreamer {

    final static private Logger logger = LoggerFactory.getLogger(CraTwitterStreamer.class);
    static CraTwitterStreamer ourInstance = new CraTwitterStreamer();
    private final DatabaseService ds = DatabaseService.getInstance();
    private StatusListener stListener;
    private Authentication auth;
    private List<String> terms;
    private long time;
    private boolean configSet = false;
    private boolean running = false;

    public static CraTwitterStreamer getInstance() {
        return ourInstance;
    }

    public void setConfig(Authentication auth, List<String> terms, long time, boolean downloadMedia) {
        // Receive OAuth params
        this.auth = auth;

        // Timing parameter
        this.time = time;

        //Setup the StatusListener
        stListener = new CraTwitterStatusListener();

        // Reset DatabaseService & set download mode
        ds.init(downloadMedia);

        // Terms for filtering
        this.terms = terms;
        logger.info("Filtering tweets with terms {}", terms.toString());

        configSet = true;
    }

    public void shutdown() {
        running = false;
    }

    public long loadAndPersistStream(int threads) throws InterruptedException, IOException {
        if (configSet) {
            // Create an appropriately sized blocking queue
            BlockingQueue<String> queue = new LinkedBlockingQueue<>(1000);

            // Create a filtered endpoint
            StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();

            // Add tracked terms
            endpoint.trackTerms(terms);

            // Create a new BasicClient. By default gzip is enabled.
            BasicClient client = new ClientBuilder()
                    .hosts(Constants.STREAM_HOST)
                    .endpoint(endpoint)
                    .authentication(auth)
                    .processor(new StringDelimitedProcessor(queue))
                    .build();


            // Create an executor service which will spawn threads to do the actual work of parsing the incoming messages and
            // calling the listeners on each message
            ExecutorService service = Executors.newFixedThreadPool(threads);

            // Wrap our BasicClient with the twitter4j client
            Twitter4jStatusClient t4jClient = new Twitter4jStatusClient(
                    client, queue, Lists.newArrayList(stListener), service);


            // Establish a connection
            t4jClient.connect();

            // Wait for network connectivity
            while (t4jClient.getStatsTracker().getNumConnects() == 0) {
                Thread.sleep(50);
            }

            if (t4jClient.isDone()) {
                throw new IOException("Endpoint unreachable");
            }

            // Start processing threads
            for (int thr = 0; thr < threads; ++thr) {
                t4jClient.process();
            }

            StatsReporter.StatsTracker tracker = t4jClient.getStatsTracker();

            // TODO: Handle disconnects https://dev.twitter.com/streaming/overview/messages-types#disconnect_messages

            // Keeping this tool alive as set up in the properties file
            if (running) {
                while (running) {
                    Thread.sleep(50);
                }
            } else {
                Thread.sleep(time);
            }

            logger.info("- # - Connects: {}, Disconnects: {}, Dropped messages: {}", tracker.getNumConnects(),
                    tracker.getNumDisconnects(), tracker.getNumMessagesDropped());

            client.stop();
            Thread.sleep(1500);

            // Wait for worker threads
            while (!client.isDone()) {
                Thread.sleep(250);
            }
            // Perform a clean shutdown
            long delta = ds.getCnt();
            DatabaseService.getInstance().shutdown();

            Thread.sleep(2500);
            logger.info("Messages dropped: {}, events dropped: {}",
                    tracker.getNumMessagesDropped(), tracker.getNumClientEventsDropped());
            return client.getStatsTracker().getNumMessages() - delta;
        } else {
            throw new NullPointerException("Config not set");
        }
    }

}

