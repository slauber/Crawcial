package de.crawcial.web.setup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.crawcial.Constants;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;
import org.lightcouch.DesignDocument;

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
    private final String DOCUSERID = "org.couchdb.user:crawcial_control";
    private final String DOCUSERNAME = "crawcial_control";
    private final String DOCUSERGROUP = DOCUSERNAME;
    private final String DOCCONFIGDB = DOCUSERNAME;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (Validator.isDbConfigured(req.getServletContext()) == 0) {
            resp.sendRedirect(Constants.HOME);
        } else {
            throwError(resp, 1020);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("action") != null && req.getParameter("action").equals("update")) {
            CouchDbClient dbClient = null;
            JsonObject userJs = null;
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
                    userJs = new JsonObject();
                    userJs.addProperty("_id", DOCUSERID);
                    userJs.addProperty("type", "user");
                    userJs.addProperty("name", DOCUSERNAME);
                    String rndPassword = new BigInteger(130, new SecureRandom()).toString(32);
                    userJs.addProperty("password", rndPassword);
                    JsonArray roles = new JsonArray();
                    roles.add(new JsonPrimitive(DOCUSERGROUP));
                    userJs.add("roles", roles);

                    try {
                        dbClient.save(userJs);
                    } catch (CouchDbException e) {
                        if (e.getMessage().equals("Conflict")) {
                            JsonObject o = dbClient.find(JsonObject.class, DOCUSERID);
                            dbClient.remove(o);
                            dbClient.save(userJs);
                        } else {
                            throwError(resp, 1010);
                        }
                    }

                    JsonObject securityJs = new JsonObject();
                    JsonObject memberJs = new JsonObject();
                    securityJs.addProperty("_id", "_security");

                    memberJs.add("roles", roles);
                    memberJs.add("names", new JsonArray());

                    securityJs.add("admins", memberJs);
                    securityJs.add("members", memberJs);

                    // Create config db and facebook / twitter db
                    dbClient.shutdown();
                    dbClient = new CouchDbClient(DOCCONFIGDB, true, protocol, host, port, user, password);
                    dbClient.save(securityJs);
                    DesignDocument designDoc = dbClient.design().getFromDesk("crawcial");
                    dbClient.design().synchronizeWithDb(designDoc);
                    dbClient.shutdown();
                    dbClient = new CouchDbClient(Constants.FACEBOOK_DB, true, protocol, host, port, user, password);
                    dbClient.save(securityJs);
                    dbClient.shutdown();
                    dbClient = new CouchDbClient(Constants.TWITTER_DB, true, protocol, host, port, user, password);
                    dbClient.save(securityJs);
                    dbClient.shutdown();

                    Properties p = new Properties();

                    p.put("dbname", "crawcial_core");
                    p.put("dbcreatedbifnotexist", String.valueOf(true));
                    p.put("dbprotocol", protocol);
                    p.put("dbhost", host);
                    p.put("dbport", String.valueOf(port));
                    p.put("dbusername", DOCUSERNAME);
                    p.put("dbpassword", rndPassword);

                    String path = getServletContext().getRealPath("/WEB-INF");
                    FileOutputStream fos = new FileOutputStream(path + "/" + Constants.CONFIG_FILE);
                    p.store(fos, null);
                    resp.sendRedirect(Constants.USERMGMT);
                } else {
                    throwError(resp, 1002);
                }
            } catch (NumberFormatException e) {
                throwError(resp, 1001);
            } catch (CouchDbException e) {
                throwError(resp, 1003);
            }
        }
    }

    private void throwError(HttpServletResponse resp, int code) throws IOException {
        resp.sendRedirect("/" + Constants.SETUP + "&e=" + code);
        resp.getWriter().println("Could not update config - Code " + code);
    }
}
