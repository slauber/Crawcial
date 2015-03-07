package de.crawcial.twitter;

/**
 * Created by Sebastian Lauber on 21.02.15.
 */

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import de.crawcial.database.DatabaseService;
import de.crawcial.database.LoadExecutor;
import org.lightcouch.CouchDbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class CraTwitterStreamer {

    final static private Logger logger = LoggerFactory.getLogger(CraTwitterStreamer.class);
    private static CraTwitterStreamer ourInstance = new CraTwitterStreamer();
    private final DatabaseService ds = DatabaseService.getInstance();
    private Authentication auth;
    private List<String> terms;
    private long time;
    private boolean configSet = false;
    private boolean running = false;

    public static CraTwitterStreamer getInstance() {
        return ourInstance;
    }

    public void setConfig(Authentication auth, List<String> terms, long time, boolean downloadMedia, CouchDbProperties properties) {
        // Receive OAuth params
        this.auth = auth;

        // Timing parameter
        this.time = time;

        // Reset DatabaseService & set download mode
        ds.init(downloadMedia, properties);

        // Terms for filtering
        this.terms = terms;
        logger.info("Filtering tweets with terms {}", terms.toString());

        configSet = true;
    }

    public void shutdown() {
        running = false;
    }

    public long loadAndPersistStream(int threads) throws InterruptedException {
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


            // Start loadExecutors
            LoadExecutor[] loadExecutors = new LoadExecutor[threads];
            ArrayList<Thread> loadExecutorThreads = new ArrayList<>();

            for (int i = 0; i < threads; ++i) {
                loadExecutors[i] = new LoadExecutor(queue, ds.getJsonObjectVector());
                loadExecutorThreads.add(new Thread(loadExecutors[i]));
            }

            Iterator<Thread> loadExecutorThreadsIt = loadExecutorThreads.iterator();
            while (loadExecutorThreadsIt.hasNext()) {
                loadExecutorThreadsIt.next().start();
            }

            // Connect the hosebird client
            client.connect();


            // Keeping this tool alive as set up in the properties file
            if (running) {
                while (running) {
                    Thread.sleep(50);
                }
            } else {
                Thread.sleep(time);
            }

            // Disconnect the hosebird client
            client.stop();

            // Shutdown loadExecutors gracefully and join their threads
            for (LoadExecutor l : loadExecutors) {
                l.shutdown();
            }

            loadExecutorThreadsIt = loadExecutorThreads.iterator();
            while (loadExecutorThreadsIt.hasNext()) {
                loadExecutorThreadsIt.next().join();
            }

            // Shutdown the database service (AttachementExecutors / WriteExecutor)
            ds.shutdown();


            // Return the num of messages - warnings (that are not persisted)
            return client.getStatsTracker().getNumMessages() - DatabaseService.getInstance().getWarningCnt();
        } else {
            throw new IllegalStateException("Config not set");
        }
    }

}

