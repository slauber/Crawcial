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

public class CraTwitterStreamer implements Runnable {

    final static private Logger logger = LoggerFactory.getLogger(CraTwitterStreamer.class);
    private static CraTwitterStreamer ourInstance = new CraTwitterStreamer();
    private final DatabaseService ds = DatabaseService.getInstance();
    private Authentication auth;
    private List<String> terms;
    private long time;
    private boolean configSet = false;
    private boolean running = false;
    private long result;
    private int threads = Runtime.getRuntime().availableProcessors();

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

    @Override
    public void run() {
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
                if (running) {
                    while (running) {
                        Thread.sleep(50);
                    }
                } else {

                    Thread.sleep(time);


                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Disconnect the hosebird client
            client.stop();

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

            // Shutdown the database service (AttachementExecutors / WriteExecutor)
            try {
                ds.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Return the num of messages - warnings (that are not persisted)
            result = client.getStatsTracker().getNumMessages() - DatabaseService.getInstance().getWarningCnt();
        } else {
            throw new IllegalStateException("Config not set");
        }
    }
}

