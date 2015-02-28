package de.crawcial.twitter.test;

import de.crawcial.twitter.CraTwitter;
import org.junit.Before;
import org.junit.Test;
import org.lightcouch.CouchDbClient;

/**
 * Created by Sebastian Lauber on 20.02.15.
 */

public class StreamingTest {
    private final String[] terms = {"21ReasonsWhyWeLoveJustin"};

    @Before
    public void setUp() throws Exception {
        // Trash old database
        CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
        String dbName = "cra-twitter-couch";
        dbClient.context().deleteDB(dbName, "delete database");
        dbClient.shutdown();
    }

    @Test
    public void streamingTestWithPersistence() throws Exception {
        // Start analysis with predefined terms
        long downloaded = CraTwitter.startAnalysis(terms, true);

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
}
