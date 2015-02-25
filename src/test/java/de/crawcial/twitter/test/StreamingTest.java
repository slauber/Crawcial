package de.crawcial.twitter.test;

import de.crawcial.twitter.CraTwitter;
import org.junit.Before;
import org.junit.Test;
import org.lightcouch.CouchDbClient;

/**
 * Created by Sebastian Lauber on 20.02.15.
 */

public class StreamingTest {
    final String dbName = "cra-twitter-couch";
    final String[] terms = {"twitter"};

    @Before
    public void setUp() throws Exception {
        CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
        dbClient.context().deleteDB(dbName, "delete database");
        dbClient.shutdown();
    }

    @Test
    public void testName() throws Exception {
        CraTwitter.startAnalysis(terms);
        CouchDbClient dbClient = new CouchDbClient("couchdb_no_new.properties");
        dbClient.context().ensureFullCommit();
    }
}
