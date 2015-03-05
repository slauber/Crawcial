package de.crawcial.twitter;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.lightcouch.CouchDbClient;
import twitter4j.TwitterException;

import java.io.IOException;

/**
 * Created by Sebastian Lauber on 20.02.15.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StreamingTest {
    private final String[] terms = {"Coulson", "BroomWar", "#QuandoCrescerEu", "photography", "#AlisScared",
            "DearMe", "#5ONTHEWALL", "#EndofEzria", "#LoUnicoQueNecesitoEs", "rip", "landscape", "beer", "party"};

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

    @Test
    public void t1est10sWithoutMedia() throws TwitterException, IOException {
        final String[] terms = {"twitter"};
        testStreaming(10000, terms, false);
    }

    @Test
    public void t2est20sWithMedia() throws TwitterException, IOException {
        testStreaming(20000, terms, true);
    }

    @Test
    public void t3est20sWithoutMedia() throws TwitterException, IOException {
        final String[] terms = {"twitter"};
        testStreaming(20000, terms, false);
    }

    @Test
    public void t4est30sWithoutMedia() throws TwitterException, IOException {
        final String[] terms = {"twitter"};
        testStreaming(30000, terms, false);
    }

    @Test
    public void t5est200sWithMedia() throws TwitterException, IOException {
        testStreaming(200000, terms, true);
    }

    @Test
    public void t6est15MinWithMedia() throws TwitterException, IOException {
        testStreaming(900000, terms, true);
    }

}
