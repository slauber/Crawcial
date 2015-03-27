package de.crawcial.web.social;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.httpclient.auth.OAuth1;
import de.crawcial.Constants;
import de.crawcial.twitter.TwitterStreamer;
import de.crawcial.web.auth.AuthHelper;
import de.crawcial.web.util.CrawcialWebUtils;
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
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This servlet manages the Crawcial for Twitter activities.
 *
 * @author Sebastian Lauber
 */
public class TwServlet extends HttpServlet {

    /**
     * Returns the running state of Crawcial for Twitter.
     *
     * @return true, if TwitterStreamer is running
     */
    public static boolean isRunning() {
        return TwitterStreamer.getInstance().isRunning();
    }

    /**
     * Returns true, if TwitterStreamer is shutting down.
     *
     * @return true, if TwitterStreamer is shutting down
     */
    public static boolean isShuttingDown() {
        return TwitterStreamer.getInstance().isRunning() && !TwitterStreamer.getInstance().isActive();
    }

    /**
     * Returns a status string, containing terms, location, start time, and runtime in seconds.
     *
     * @return status string, containing terms, location, start time, and runtime in seconds
     */
    public static String getStatus() {
        List<String> terms = TwitterStreamer.getInstance().getTerms();
        Date startTime = TwitterStreamer.getInstance().getStartDate();
        long currentCnt = TwitterStreamer.getInstance().getResult();
        List<Location> locations = TwitterStreamer.getInstance().getLocations();
        if (terms != null && startTime != null) {
            DateFormat df = DateFormat.getInstance();
            long runtime = System.currentTimeMillis() - startTime.getTime();
            return "Filtering tweets by terms: " + terms.toString() + " or Location: " + locations + " - Started at: " + df.format(startTime) +
                    "(active for " + runtime / 1000 + " seconds) - Current item count " + currentCnt;
        }
        return "Crawcial for Twitter initializing...";
    }

    /**
     * Returns a base64 encoded string with current worldwide Twitter trends.
     *
     * @param req the http request
     * @return base64 encoded string with current worldwide Twitter trends
     */
    public static String getTrendingWorldwideBase64(HttpServletRequest req) {
        Twitter twitter = TwitterFactory.getSingleton();
        try {
            Map<String, String> socialKeys = Tokenmanager.getSocialToken(req);
            try {
                twitter.setOAuthConsumer(socialKeys.get("twconsumerkey"), socialKeys.get("twconsumersecret"));
                twitter.setOAuthAccessToken(Tokenmanager.getTwitter4jAccessToken(req));
            } catch (IllegalStateException e) {
                // Macht nix
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

    /**
     * Returns true, if the media downloader has been disabled due to low memory.
     *
     * @return true, if the media downloader has been disabled due to low memory
     */
    public static boolean isLowMemory() {
        return TwitterStreamer.getInstance().isLowMemory();
    }

    /**
     * Prefills the terms field of Crawcial for Twitter with the current world wide trends.
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter(Constants.ACTION) != null && req.getParameter(Constants.ACTION).equals("trends")) {
            resp.sendRedirect(Constants.TWITTER + "&terms=" + getTrendingWorldwideBase64(req));
        } else {
            resp.sendRedirect(Constants.TWITTER);
        }
    }

    /**
     * Configures and starts TwitterStreamer or shuts it down.
     * <p>request parameter: action, terms, geo, ne, sw, media, mediahttps, imgsize, </p>
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthHelper.isAuthenticated(req) && req.getParameter("action") != null) {
            TwitterStreamer crs = TwitterStreamer.getInstance();
            String actionParam = req.getParameter(Constants.ACTION);
            switch (actionParam) {
                case "persist":
                    if (!TwitterStreamer.getInstance().isRunning() && !TwitterStreamer.getInstance().isActive()) {
                        CouchDbProperties dbProperties = CrawcialWebUtils.getCouchDbProperties(req.getServletContext(), Constants.TWITTER_DB);
                        String termString = req.getParameter("terms");
                        List<String> terms = null;
                        if (termString != null) {
                            byte[] bytes = termString.getBytes(StandardCharsets.ISO_8859_1);
                            termString = new String(bytes, StandardCharsets.UTF_8);
                            terms = Arrays.asList(termString.split("\\s*,\\s*"));
                        }
                        OAuth1 oauth = null;
                        try {
                            oauth = Tokenmanager.getTwitterOAuth(req);
                        } catch (URISyntaxException e) {
                            // Macht nix
                        }
                        if (oauth != null && dbProperties != null && terms != null) {
                            Location l = null;
                            String ne = null;
                            String sw = null;
                            if (req.getParameter("geo") != null && req.getParameter("geo").equals("true")) {
                                ne = req.getParameter("ne");
                                sw = req.getParameter("sw");

                                Location.Coordinate neC = new Location.Coordinate(Float.valueOf(ne.substring(ne.indexOf(" ")
                                        + 1, ne.indexOf(")"))), Float.valueOf(ne.substring(1, ne.indexOf(","))));

                                Location.Coordinate swC = new Location.Coordinate(Float.valueOf(sw.substring(sw.indexOf(" ")
                                        + 1, sw.indexOf(")"))), Float.valueOf(sw.substring(1, sw.indexOf(","))));

                                l = new Location(swC, neC);
                            }
                            crs.setConfig(oauth, terms, Boolean.valueOf(req.getParameter("media")), dbProperties,
                                    req.getParameter("imgsize"), Boolean.valueOf(req.getParameter("mediahttps")), l);
                            Thread t = new Thread(crs);
                            t.setName("TwitterStreamerMain");
                            CouchDbProperties masterDbProperties = CrawcialWebUtils.getCouchDbProperties(req.getServletContext(), Constants.CONFIGDB);
                            //noinspection ConstantConditions
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
                            if (l != null) {
                                j.addProperty("locNE", ne);
                                j.addProperty("locSW", sw);
                            }
                            if (Boolean.valueOf(req.getParameter("media"))) {
                                j.addProperty("media", req.getParameter("imgsize"));
                            }
                            j.addProperty("_id", "twitter");
                            JsonArray jTerms = new JsonArray();
                            for (String term : terms) {
                                jTerms.add(new JsonPrimitive(term));
                            }
                            j.add("terms", jTerms);
                            j.addProperty("start", String.valueOf(System.currentTimeMillis()));
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            j.addProperty("startHumanReadable", sdf.format(new Date(System.currentTimeMillis())));
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
                case "shutdownNow":
                    crs.forceShutdown();
                    break;
                default:
                    break;
            }
            resp.sendRedirect(Constants.TWITTER);
        }
    }
}
