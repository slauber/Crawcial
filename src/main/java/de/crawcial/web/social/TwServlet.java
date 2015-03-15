package de.crawcial.web.social;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.twitter.hbc.httpclient.auth.OAuth1;
import de.crawcial.Constants;
import de.crawcial.twitter.CraTwitterStreamer;
import de.crawcial.web.auth.AuthHelper;
import de.crawcial.web.util.Modules;
import de.crawcial.web.util.Tokenmanager;
import org.apache.commons.codec.binary.Base64;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import org.lightcouch.NoDocumentException;
import twitter4j.Trend;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.*;

/**
 * Created by Sebastian Lauber on 11.03.2015.
 */
public class TwServlet extends HttpServlet {

    public static boolean isRunning() {
        return CraTwitterStreamer.getInstance().isRunning();
    }

    public static boolean isShuttingDown() {
        return CraTwitterStreamer.getInstance().isRunning() && !CraTwitterStreamer.getInstance().isActive();
    }


    public static String getStatus() {
        List<String> terms = CraTwitterStreamer.getInstance().getTerms();
        Date startTime = CraTwitterStreamer.getInstance().getStartDate();
        long currentCnt = CraTwitterStreamer.getInstance().getResult();
        if (terms != null && startTime != null) {
            DateFormat df = DateFormat.getInstance();
            long runtime = System.currentTimeMillis() - startTime.getTime();
            return "Filtering tweets by terms: " + terms.toString() + " - Started at: " + df.format(startTime) +
                    "(active for " + runtime / 1000 + " seconds) - Current item count " + currentCnt;
        }
        return "CraTwitter initializing...";
    }

    public static String getTrendingWorldwideBase64(HttpServletRequest req) {
        Twitter twitter = TwitterFactory.getSingleton();
        try {
            Map<String, String> socialKeys = Tokenmanager.getSocialToken(req);
            try {
                twitter.setOAuthConsumer(socialKeys.get("twconsumerkey"), socialKeys.get("twconsumersecret"));
                twitter.setOAuthAccessToken(Tokenmanager.getTwitter4jAccessToken(req));
            } catch (IllegalStateException e) {

            }
            Trend[] trends = twitter.getPlaceTrends(1).getTrends();
            StringBuilder sb = new StringBuilder();
            for (Trend t : trends) {
                if (!t.getName().contains("?")) {
                    sb.append(t.getName());
                    sb.append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            String s = sb.toString();
            return Base64.encodeBase64URLSafeString(s.trim().getBytes(Charset.forName("UTF-8")));
        } catch (TwitterException e) {
            if (e.getStatusCode() == 429) {
                return Base64.encodeBase64URLSafeString("Rate limit exceeded".getBytes());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("action") != null && req.getParameter("action").equals("trends")) {
            resp.sendRedirect(Constants.TWITTER + "&trends=" + getTrendingWorldwideBase64(req));
        } else {
            resp.sendRedirect(Constants.TWITTER);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthHelper.isAuthenticated(req) && req.getParameter("action") != null) {
            CraTwitterStreamer crs = CraTwitterStreamer.getInstance();
            switch (req.getParameter("action")) {
                case "persist":
                    if (!CraTwitterStreamer.getInstance().isRunning() && !CraTwitterStreamer.getInstance().isActive()) {
                        CouchDbProperties dbProperties = Modules.getCouchDbProperties(req.getServletContext(), Constants.TWITTER_DB);
                        List<String> terms = req.getParameter("terms") == null ? null : Arrays.asList(req.getParameter("terms").split("\\s*,\\s*"));
                        OAuth1 oauth = null;
                        try {
                            oauth = Tokenmanager.getTwitterOAuth(req);
                        } catch (URISyntaxException e) {
                        }
                        if (oauth != null && dbProperties != null && terms != null) {
                            crs.setConfig(oauth, terms, Boolean.valueOf(req.getParameter("media")), dbProperties);
                            Thread t = new Thread(crs);
                            t.setName("Master-of-desaster");
                            CouchDbProperties masterDbProperties = Modules.getCouchDbProperties(req.getServletContext(), Constants.CONFIGDB);
                            CouchDbClient dbClient = new CouchDbClient(masterDbProperties);
                            JsonObject j;
                            boolean existed;
                            try {
                                j = dbClient.find(JsonObject.class, "twitter");
                                existed = true;
                            } catch (NoDocumentException e) {
                                j = new JsonObject();
                                existed = false;
                            }
                            j.addProperty("_id", "twitter");
                            JsonArray jTerms = new JsonArray();
                            Iterator<String> termsIt = terms.iterator();
                            while (termsIt.hasNext()) {
                                jTerms.add(new JsonPrimitive(termsIt.next()));
                            }
                            j.add("terms", jTerms);
                            j.addProperty("start", String.valueOf(System.currentTimeMillis()));
                            DateFormat df = DateFormat.getDateInstance();
                            j.addProperty("startHumanReadable", df.format(new Date(System.currentTimeMillis())));
                            if (existed) {
                                dbClient.update(j);
                            } else {
                                dbClient.save(j);
                            }
                            t.start();
                        }
                    }
                    break;
                case "shutdown":
                    crs.shutdown();
                    break;
                default:
                    break;
            }
            resp.sendRedirect(Constants.TWITTER);
        }
    }
}
