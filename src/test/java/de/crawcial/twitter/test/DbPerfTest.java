package de.crawcial.twitter.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.lightcouch.CouchDbClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Lauber on 28.02.15.
 */
public class DbPerfTest {
    @Test
    public void dbBenchmark() throws Exception {
        // Run each benchmark four times in order to remove peaks
        assert (bench(250) + bench(250) + bench(250) + bench(250) < 2000);
        assert (bench(1000) + bench(1000) + bench(1000) + bench(1000) < 4000);
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

    List<JsonObject> generateSampleData(int amount) {
        // Generate JSON data similar to real tweets
        ArrayList<JsonObject> testList = new ArrayList<>(amount);
        for (int j = 0; j < amount; j++) {
            JsonObject js = new JsonObject();
            JsonArray array1 = new JsonArray();
            JsonArray array2 = new JsonArray();
            js.addProperty("val1", String.valueOf(Math.random() * 500000));
            js.addProperty("val2", String.valueOf(Math.random() * 500000));
            js.addProperty("val3", String.valueOf(Math.random() * 500000));
            for (int i = 0; i < Math.random() * 2; ++i) {
                JsonObject arrayObject = new JsonObject();
                arrayObject.addProperty("val1", String.valueOf(Math.random() * 500000));
                arrayObject.addProperty("val2", String.valueOf(Math.random() * 500000));
                array1.add(arrayObject);
                array2.add(arrayObject);
            }
            js.add("array1", array1);
            js.add("array2", array2);
            testList.add(js);
        }
        return testList;
    }
}
