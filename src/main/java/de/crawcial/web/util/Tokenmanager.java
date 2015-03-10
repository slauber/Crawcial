package de.crawcial.web.util;

import com.google.gson.JsonObject;
import de.crawcial.web.auth.AuthHelper;
import org.lightcouch.CouchDbClient;
import org.lightcouch.NoDocumentException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sebastian Lauber on 10.03.2015.
 */
public class Tokenmanager extends HttpServlet {
    final static String[] keys = {"fbappid", "fbappsecret", "twconsumerkey", "twconsumersecret"};

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthHelper.isAuthenticated(req) && req.getParameter("action") != null || req.getParameter("action").equals("update")) {

            CouchDbClient dbClient = Modules.getDbClient(getServletContext(), Modules.CONFIGDB);
            JsonObject social;
            try {
                social = dbClient.find(JsonObject.class, Modules.SOCIAL_KEYS);
            } catch (NoDocumentException e) {
                social = new JsonObject();
            }

            social.addProperty("_id", Modules.SOCIAL_KEYS);
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
            resp.sendRedirect(Modules.DASHBOARD_CONFIG);
        } else {
            resp.sendRedirect(Modules.HOME);
        }
    }

    public static Map<String, String> getSocialToken(HttpServletRequest req) throws IOException {
        CouchDbClient dbClient = Modules.getDbClient(req.getServletContext(), Modules.CONFIGDB);
        Map<String, String> values = new HashMap<>();
        try {
            JsonObject o = dbClient.find(JsonObject.class, Modules.SOCIAL_KEYS);
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
}