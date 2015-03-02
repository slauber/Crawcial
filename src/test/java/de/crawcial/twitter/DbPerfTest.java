package de.crawcial.twitter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.lightcouch.CouchDbClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Lauber on 28.02.15.
 */
public class DbPerfTest {

    public static List<JsonObject> generateSampleData(int amount) {
        // Generate JSON data similar to real tweets
        ArrayList<JsonObject> testList = new ArrayList<>(amount);
        for (int j = 0; j < amount; j++) {
            JsonObject js = new JsonObject();
            JsonArray array1 = new JsonArray();
            JsonArray array2 = new JsonArray();
            js.addProperty("links", String.valueOf(Math.random() * 500000));
            js.addProperty("val2", String.valueOf(Math.random() * 500000));
            js.addProperty("val3", String.valueOf(Math.random() * 500000));
            for (int i = 1; i < Math.random() * 2; ++i) {
                JsonObject arrayObject = new JsonObject();
                arrayObject.addProperty("url", String.valueOf(Math.random() * 500000));
                arrayObject.addProperty("val2", String.valueOf(Math.random() * 500000));
                array1.add(arrayObject);
                array2.add(arrayObject);
            }

            JsonArray jsLinks = new JsonArray();
            JsonObject jsMediaO = new JsonObject();
            jsMediaO.addProperty("type", "image");
            jsMediaO.addProperty("url", "https://example.com/loremipsumdolorsitamet");
            jsLinks.add(jsMediaO);

            js.add("media", jsLinks);
            js.add("array1", array1);
            js.add("array2", array2);
            testList.add(js);
        }
        return testList;
    }

    @Before
    public void warmUp() throws InterruptedException {
        for (int i = 0; i < 10; ++i) {
            bench((int) (Math.random() * 2000));
        }
    }

    @Test(timeout = 4000)
    public void dbBenchmark250() throws Exception {
        // Run each benchmark 5 times in order to remove peaks
        for (int i = 0; i < 5; ++i) {
            bench(250);
        }
    }

    @Test(timeout = 7000)
    public void dbBenchmark1000() throws Exception {
        // Run each benchmark 5 times in order to remove peaks
        for (int i = 0; i < 5; ++i) {
            bench(1000);
        }
    }

    long bench(int amount) {
        // Open database connection and take a timestamp
        CouchDbClient dbClient = new CouchDbClient("benchmark.properties");
        long startTime = System.currentTimeMillis();

        // Send generated JSON samples in bulk to database
        dbClient.bulk(generateSampleData(amount), true);

        // Take a second timestamp and perform some cleanup
        long duration = System.currentTimeMillis() - startTime;
        dbClient.context().ensureFullCommit();
        dbClient.context().deleteDB("cra-twitter-couch-benchmark", "delete database");
        dbClient.shutdown();
        return duration;
    }
}
