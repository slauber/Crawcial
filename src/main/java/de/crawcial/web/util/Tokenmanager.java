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
 * Created by Sebastian Lauber on 10.03.2015.
 */
public class Tokenmanager extends HttpServlet {
    private final static String[] keys = {"fbappid", "fbappsecret", "twconsumerkey", "twconsumersecret"};

    public static Map<String, String> getSocialToken(HttpServletRequest req) throws IOException {
        CouchDbClient dbClient = new CouchDbClient(Modules.getCouchDbProperties(req.getServletContext(), Constants.CONFIGDB));
        Map<String, String> values = new HashMap<>();
        try {
            JsonObject o = dbClient.find(JsonObject.class, Constants.SOCIAL_KEYS);
            for (String k : keys) {
                if (o.has(k)) {
                    values.put(k, o.get(k).getAsString());
                }
            }
        } catch (NoDocumentException e) {
            System.out.println("No social config found");
        }
        return values;
    }

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

    public static facebook4j.auth.AccessToken getFacebookAccessToken(HttpServletRequest req) {
        try {
            return new facebook4j.auth.AccessToken(getTokenFromCookie(req, "fbtoken"));
        } catch (NullPointerException e) {
            return null;
        }
    }

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


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthHelper.isAuthenticated(req) && req.getParameter("action") != null || req.getParameter("action").equals("update")) {

            CouchDbClient dbClient = new CouchDbClient(Modules.getCouchDbProperties(getServletContext(), Constants.CONFIGDB));
            JsonObject social;
            try {
                social = dbClient.find(JsonObject.class, Constants.SOCIAL_KEYS);
            } catch (NoDocumentException e) {
                social = new JsonObject();
            }

            social.addProperty("_id", Constants.SOCIAL_KEYS);
            for (String k : keys) {
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