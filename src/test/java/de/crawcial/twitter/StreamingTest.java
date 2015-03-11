package de.crawcial.twitter;

import de.crawcial.database.util.CouchDbCloneClient;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

/**
 * Created by Sebastian Lauber on 20.02.15.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StreamingTest {
    private final String[] terms = {"Coulson", "BroomWar", "#QuandoCrescerEu", "photography", "#AlisScared",
            "DearMe", "#5ONTHEWALL", "#EndofEzria", "#LoUnicoQueNecesitoEs", "rip", "landscape", "beer", "party"};

    void removeDb(CouchDbProperties properties) {
        // Trash old database
        CouchDbClient dbClient = new CouchDbCloneClient(properties);
        dbClient.context().deleteDB(properties.getDbName(), "delete database");
        dbClient.shutdown();
    }
/*
    void testStreaming(long millis, String[] terms, boolean downloadMedia, String name) throws TwitterException, IOException {
        // Setup database properties from template and adapt to fit
        CouchDbProperties properties = CouchDBPropertiesSource.loadFromFile("couchdb_new.properties");
        properties.setDbName(name);

        // Trash old database
        removeDb(properties);

        // Start analysis with predefined terms
        long downloaded = CraTwitter.startAnalysis(terms, downloadMedia, millis, properties);

        // Create new CouchDBProperties from template
        properties.setCreateDbIfNotExist(false);

        // Query for all documents and get document count
        CouchDbClient dbClient = new CouchDbCloneClient(properties);
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
        testStreaming(10000, terms, false, "test1");
    }

    @Test
    public void t2est120sWithMedia() throws TwitterException, IOException {
        testStreaming(120000, terms, true, "test2");
    }

    @Test
    public void t3est20sWithoutMedia() throws TwitterException, IOException {
        final String[] terms = {"twitter"};
        testStreaming(20000, terms, false, "test3");
    }

    @Test
    public void t4est30sWithoutMedia() throws TwitterException, IOException {
        final String[] terms = {"twitter"};
        testStreaming(30000, terms, false, "test4");
    }

    @Test
    public void t5est200sWithMedia() throws TwitterException, IOException {
        testStreaming(200000, terms, true, "test5");
    }

    @Test
    public void t6est15MinWithMedia() throws TwitterException, IOException {
        testStreaming(900000, terms, true, "test6");
    }
    */
}
