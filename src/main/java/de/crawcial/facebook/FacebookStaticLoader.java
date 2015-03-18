package de.crawcial.facebook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.Page;
import facebook4j.auth.AccessToken;
import facebook4j.json.DataObjectFactory;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

/**
 * Created by Sebastian Lauber on 18.03.2015.
 */
public class FacebookStaticLoader {
    private static FacebookStaticLoader ourInstance = new FacebookStaticLoader();
    private static Facebook fb;
    private static AccessToken token;

    private FacebookStaticLoader() {

    }

    public static FacebookStaticLoader getInstance() {
        return ourInstance;
    }

    public void setFbVars(Facebook fb, AccessToken token) {
        FacebookStaticLoader.fb = fb;
        FacebookStaticLoader.token = token;
    }

    public synchronized void downloadPage(String pageId, CouchDbProperties dbProperties) throws FacebookException {
        Thread t = new Thread(new LoaderThread(pageId, dbProperties));
        t.start();
        fb.setOAuthAccessToken(token);

    }

    class LoaderThread implements Runnable {
        final String pageId;
        final CouchDbProperties dbProperties;
        final JsonParser parser = new JsonParser();

        public LoaderThread(String pageId, CouchDbProperties dbProperties) {
            this.pageId = pageId;
            this.dbProperties = dbProperties;
        }

        @Override
        public void run() {
            try {
                CouchDbClient dbClient = new CouchDbClient(dbProperties);
                String rawFeed = DataObjectFactory.getRawJSON(fb.getFeed(pageId));
                System.out.println(rawFeed);

                JsonArray feedJson = (JsonArray) parser.parse(rawFeed);

                Page page = fb.getPage(pageId);
                String rawPage = DataObjectFactory.getRawJSON(page);
                System.out.println(rawPage);

                JsonObject o = (JsonObject) parser.parse(rawPage);
                o.add("feed", feedJson);
                o.addProperty("_id", page.getId());
                try {
                    JsonObject pageJson = dbClient.find(JsonObject.class, page.getId());
                    String rev = pageJson.get("_rev").getAsString();
                    o.addProperty("_rev", rev);
                    dbClient.update(o);
                } catch (Exception e) {
                    dbClient.save(o);
                }
            } catch (FacebookException e) {
                e.printStackTrace();
            }
        }
    }
}
