package de.crawcial.web.util;

import com.google.gson.JsonObject;
import com.twitter.hbc.httpclient.auth.OAuth1;
import de.crawcial.Constants;
import de.crawcial.web.auth.AuthHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.lightcouch.CouchDbClient;
import org.lightcouch.NoDocumentException;
import twitter4j.auth.AccessToken;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * This servlet manages the social media OAuth tokens.
 *
 * @author Sebastian Lauber
 */
public class Tokenmanager extends HttpServlet {
    /**
     * Returns all OAuth tokens available in the database.
     *
     * @param req the http request
     * @return all OAuth tokens available in the database
     * @throws IOException if configuration cannot be accessed
     */
    public static Map<String, String> getSocialToken(HttpServletRequest req) throws IOException {
        //noinspection ConstantConditions
        CouchDbClient dbClient = new CouchDbClient(CrawcialWebUtils.getCouchDbProperties(req.getServletContext(), Constants.CONFIGDB));
        Map<String, String> values = new HashMap<>();
        try {
            JsonObject o = dbClient.find(JsonObject.class, Constants.SOCIAL_KEYS);
            for (String k : Constants.keys) {
                if (o.has(k)) {
                    values.put(k, o.get(k).getAsString());
                }
            }
        } catch (NoDocumentException | NullPointerException e) {
            System.out.println("No social config found");
        }
        return values;
    }

    /**
     * Returns a hbc OAuth user token from database (consumer token) and cookie values (user token).
     *
     * @param req the http request
     * @return Hosebird Client OAuth credentials (from user token)
     * @throws IOException        if configuration cannot be accessed
     * @throws URISyntaxException if a malformed URL is provided
     */
    public static OAuth1 getTwitterOAuth(HttpServletRequest req) throws IOException, URISyntaxException {
        Map<String, String> socialToken = getSocialToken(req);

        String accessToken = null;
        String accessTokenSecret = null;

        for (Cookie c : req.getCookies()) {
            if (c.getName().equals("twtoken")) {
                String cookieString = new String(Base64.decodeBase64(c.getValue()));
                accessToken = cookieString.substring(0, cookieString.indexOf(" "));
                accessTokenSecret = cookieString.substring(cookieString.indexOf(" -") + 3);
            }
        }

        if (accessToken == null) {
            return null;
        }

        OAuth1 token = new OAuth1(socialToken.get("twconsumerkey"), socialToken.get("twconsumersecret"), accessToken, accessTokenSecret);
        URI u = new URI("https", null, "api.twitter.com", -1, "/1.1/application/rate_limit_status.json", "resources=help,users,search,statuses", null);

        HttpGet g = new HttpGet(u);

        try {
            token.signRequest(g, "");
        } catch (Exception e) {
            return null;
        }

        HttpClient c = HttpClients.createMinimal();
        HttpResponse r = c.execute(g);
        if (r.getStatusLine().getStatusCode() < 400) {
            return token;
        }
        return null;
    }

    /**
     * Returns a twitter4j OAuth user token from database (consumer token) and cookie values (user token).
     *
     * @param req the http request
     * @return twitter4j OAuth credentials (from user token)
     */
    @SuppressWarnings("ConstantConditions")
    public static twitter4j.auth.AccessToken getTwitter4jAccessToken(HttpServletRequest req) {
        String accessToken = null;
        String accessTokenSecret = null;

        for (Cookie c : req.getCookies()) {
            if (c.getName().equals("twtoken")) {
                String cookieString = new String(Base64.decodeBase64(c.getValue()));
                accessToken = cookieString.substring(0, cookieString.indexOf(" "));
                accessTokenSecret = cookieString.substring(cookieString.indexOf(" -") + 3);
            }
        }
        return new AccessToken(accessToken, accessTokenSecret);
    }

    /**
     * Returns a facebook4j OAuth user token from database (consumer token) and cookie values (user token).
     *
     * @param req the http request
     * @return facebook4j OAuth credentials (from user token)
     */
    public static facebook4j.auth.AccessToken getFacebookAccessToken(HttpServletRequest req) {
        try {
            //noinspection ConstantConditions
            return new facebook4j.auth.AccessToken(getTokenFromCookie(req, "fbtoken"));
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Returns the value of a specific cookie.
     *
     * @param req  the http request
     * @param name the cookie name
     * @return value of the specified cookie
     */
    @SuppressWarnings("SameParameterValue")
    private static String getTokenFromCookie(HttpServletRequest req, String name) {
        Cookie[] c = req.getCookies();
        if (c != null) {
            for (Cookie cs : c) {
                if (cs.getName().equals(name)) {
                    return new String(Base64.decodeBase64(cs.getValue()));
                }
            }
        }
        return null;
    }

    /**
     * Updates the social token configuration document in the Crawcial control database.
     * <p>request parameter: action</p>
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthHelper.isAuthenticated(req) && req.getParameter("action") != null || req.getParameter("action").equals("update")) {

            //noinspection ConstantConditions
            CouchDbClient dbClient = new CouchDbClient(CrawcialWebUtils.getCouchDbProperties(getServletContext(), Constants.CONFIGDB));
            JsonObject social;
            try {
                social = dbClient.find(JsonObject.class, Constants.SOCIAL_KEYS);
            } catch (NoDocumentException e) {
                social = new JsonObject();
            }

            social.addProperty("_id", Constants.SOCIAL_KEYS);
            for (String k : Constants.keys) {
                if (req.getParameter(k) != null) {
                    social.addProperty(k, req.getParameter(k));
                }
            }
            if (social.has("_rev")) {
                dbClient.update(social);
            } else {
                dbClient.save(social);
            }
            resp.sendRedirect(Constants.CONFIGURATION);
        } else {
            resp.sendRedirect(Constants.HOME);
        }
    }
}