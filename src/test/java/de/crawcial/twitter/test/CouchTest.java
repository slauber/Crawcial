package de.crawcial.twitter.test;

import com.google.gson.JsonObject;
import org.junit.Test;
import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;

/**
 * Created by Sebastian Lauber on 22.02.15.
 */
public class CouchTest {
    @Test
    public void testDatabaseConnection() throws Exception {
        CouchDbClient dbClient;
        dbClient = new CouchDbClient("couchdb.properties");
        JsonObject json = new JsonObject();
        json.addProperty("test", "content");
        Response response = dbClient.save(json);
        assert dbClient.contains(response.getId());
        dbClient.remove(response.getId(), response.getRev());
        assert !dbClient.contains(response.getId());
    }
}
