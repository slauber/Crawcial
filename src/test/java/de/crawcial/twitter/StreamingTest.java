package de.crawcial.twitter;

import org.junit.Test;
import org.lightcouch.CouchDbClient;
import twitter4j.TwitterException;

import java.io.IOException;

/**
 * Created by Sebastian Lauber on 20.02.15.
 */


public class StreamingTest {
    void removeDb(String name) {


        // Trash old database
        CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
        dbClient.context().deleteDB(name, "delete database");
        dbClient.shutdown();
    }

    void testStreaming(long millis, String[] terms, boolean downloadMedia) throws TwitterException, IOException {
        // Trash old database
        removeDb("cra-twitter-couch");

        // Start analysis with predefined terms
        long downloaded = CraTwitter.startAnalysis(terms, downloadMedia, millis);

        // Query for all documents and get document count
        CouchDbClient dbClient = new CouchDbClient("couchdb_no_new.properties");
        dbClient.context().ensureFullCommit();

        // -1 for the design document
        long db = dbClient.context().info().getDocCount() - 1;
        System.out.println("Crawler: " + downloaded);
        System.out.println("DB: " + db);

        // Test whether all downloaded tweets are persisted
        assert (downloaded == db);
    }

    /*
        @Test
        public void test100000WithoutMedia() throws TwitterException, IOException {
            final String[] terms = {"twitter"};
            testStreaming(100000, terms, false);
        }

        @Test
        public void test10000WithMedia() throws TwitterException, IOException {
            final String[] terms = {"#SanaAwareKa"};
            testStreaming(10000, terms, true);
        }

        @Test
        public void test20000WithoutMedia() throws TwitterException, IOException {
            final String[] terms = {"#SanaAwareKa"};
            testStreaming(20000, terms, false);
        }
    */
    @Test
    public void test20000WithMedia() throws TwitterException, IOException {
        final String[] terms = {"twitter"};
        testStreaming(200000, terms, true);
    }
/*
    @Test
    public void test40000WithoutMedia() throws TwitterException, IOException {
        final String[] terms = {"twitter"};
        testStreaming(40000, terms, false);
    }
*/
}
