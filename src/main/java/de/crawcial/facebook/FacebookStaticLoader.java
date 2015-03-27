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
 * This singleton handles the one time crawl requests for Facebook pages.
 *
 * @author Sebastian Lauber
 */
public class FacebookStaticLoader {
    private static FacebookStaticLoader ourInstance = new FacebookStaticLoader();
    private static Facebook fb;
    private static AccessToken token;

    private FacebookStaticLoader() {
    }

    /**
     * Returns the FacebookStaticLoader singleton.
     *
     * @return FacebookStaticLoader singleton
     */
    public static FacebookStaticLoader getInstance() {
        return ourInstance;
    }

    /**
     * This method sets the facebook4j client and the user access token.
     *
     * @param fb    preconfigured facebook4j client
     * @param token Facebook user access token
     */
    public void setFbVars(Facebook fb, AccessToken token) {
        FacebookStaticLoader.fb = fb;
        FacebookStaticLoader.token = token;
    }

    /**
     * Invokes the download process.
     *
     * @param pageId       the page ID
     * @param dbProperties CouchDbProperties pointing to the Crawcial Facebook database
     * @throws FacebookException if an error occurred during accessing Facebook
     */
    public synchronized void downloadPage(String pageId, CouchDbProperties dbProperties) throws FacebookException {
        Thread t = new Thread(new LoaderThread(pageId, dbProperties));
        t.start();
        fb.setOAuthAccessToken(token);

    }

    /**
     * Class for loader threads for multithreading.
     *
     * @author Sebastian Lauber
     */
    class LoaderThread implements Runnable {
        final String pageId;
        final CouchDbProperties dbProperties;
        final JsonParser parser = new JsonParser();

        /**
         * Constructor sets the Facebook page ID and CouchDbProperties.
         *
         * @param pageId       the page ID
         * @param dbProperties CouchDbProperties pointing to the Crawcial Facebook database
         */
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
