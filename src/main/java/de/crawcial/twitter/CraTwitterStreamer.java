package de.crawcial.twitter;

/**
 * Created by Sebastian Lauber on 21.02.15.
 */

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.twitter4j.Twitter4jStatusClient;
import org.lightcouch.CouchDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.StatusListener;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class CraTwitterStreamer {

    final static private Logger logger = LoggerFactory.getLogger(CraTwitterStreamer.class);
    private final CouchDbClient dbClient;
    // A bare bones StatusStreamHandler, which extends listener and gives some extra functionality
    private final StatusListener listener1;
    private Authentication auth;
    private List<String> terms;
    private int time;
    private int reps;

    public CraTwitterStreamer(Authentication auth, List<String> terms, int time, int reps) {
        // Receive OAuth params
        this.auth = auth;

        // Timing parameters
        this.time = time;
        this.reps = reps;

        //Setup CouchDB
        dbClient = new CouchDbClient("couchdb.properties");

        //Setup the StatusListener
        listener1 = new CraTwitterStatusListener(dbClient);

        // Terms for filtering
        this.terms = terms;
        logger.info("Filtering tweets with terms {}", terms.toString());
    }

    public void oauth() throws InterruptedException {
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
        int numProcessingThreads = 8;
        ExecutorService service = Executors.newFixedThreadPool(numProcessingThreads);

        // Wrap our BasicClient with the twitter4j client
        Twitter4jStatusClient t4jClient = new Twitter4jStatusClient(
                client, queue, Lists.newArrayList(listener1), service);


        // Establish a connection
        t4jClient.connect();
        for (int thr = 0; thr < numProcessingThreads; ++thr) {
            // This must be called once per processing thread
            t4jClient.process();
        }

        // Keeping this tool alive as set up in the properties file
        for (int i = 0; i < reps; ++i) {
            Thread.sleep(time);
            logger.info("- Current message count: {}", client.getStatsTracker().getNumMessages());
        }
        client.stop();
        Thread.sleep(2500);
        dbClient.shutdown();
    }

}

