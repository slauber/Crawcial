package de.crawcial.web.setup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.crawcial.web.Modules;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Properties;

/**
 * Created by Sebastian Lauber on 09.03.15.
 */
public class Updateconfig extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("action") != null && req.getParameter("action").equals("update")) {
            CouchDbClient dbClient = null;
            try {
                // Verify, whether valid connection information are in place
                int currentCode = Validator.isDbConfigured(getServletContext());
                int prevCode = Integer.valueOf(req.getParameter("code"));

                // Only if provided reason and current system state match, do the code
                if (currentCode != 0 && currentCode == prevCode) {
                    // Do the update
                    String name = "_users";
                    String protocol = req.getParameter("protocol");
                    String host = req.getParameter("host");
                    int port = Integer.valueOf(req.getParameter("port"));
                    String user = req.getParameter("user");
                    String password = req.getParameter("password");

                    dbClient = new CouchDbClient(name, true, protocol, host, port, user, password);

                    // Create a new user with random password
                    JsonObject userJs = new JsonObject();
                    userJs.addProperty("_id", "org.couchdb.user:crawcial_control");
                    userJs.addProperty("type", "user");
                    userJs.addProperty("name", "crawcial_control");
                    String rndPassword = new BigInteger(130, new SecureRandom()).toString(32);
                    userJs.addProperty("password", rndPassword);
                    JsonArray roles = new JsonArray();
                    roles.add(new JsonPrimitive("crawcial_control"));
                    userJs.add("roles", roles);

                    dbClient.save(userJs);

                    Properties p = new Properties();

                    p.put("dbname", "crawcial-core");
                    p.put("dbcreatedbifnotexist", String.valueOf(true));
                    p.put("dbprotocol", protocol);
                    p.put("dbhost", host);
                    p.put("dbport", String.valueOf(port));
                    p.put("dbusername", user);
                    p.put("dbpassword", password);


                    String path = getServletContext().getRealPath("/WEB-INF");
                    FileOutputStream fos = new FileOutputStream(path + "/" + Validator.CONFIG_FILE);
                    p.store(fos, null);

                } else {
                    throwError(resp, 1002);
                }
            } catch (NumberFormatException e) {
                throwError(resp, 1001);
            } catch (CouchDbException e) {
                if (e.getMessage().equals("Conflict")) {
                }
                throwError(resp, 1010);
            }
        }
    }

    private void throwError(HttpServletResponse resp, int code) throws IOException {
        resp.sendRedirect("/" + Modules.SETUP + "&e=" + code);
        resp.getWriter().println("Could not update config - Code " + code);
    }
}
