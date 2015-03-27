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
 * Database configuration servlet, provides setup API for Crawcial.
 *
 * @author Sebastian Lauber
 */
public class Updateconfig extends HttpServlet {
    /**
     * Checks for the database configuration and redirects to the homepage of Crawcial.
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (Validator.isDbConfigured(req.getServletContext()) == 0) {
            resp.sendRedirect(Constants.HOME);
        } else {
            throwError(resp, 1020);
        }
    }

    /**
     * Handles the setup process, checks for existing configuration and errors.
     * <p>request parameter: action, host, protocol, user, password, code</p>
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("action") != null && req.getParameter("action").equals("update")) {
            @SuppressWarnings("UnusedAssignment")
            CouchDbClient dbClient = null;
            @SuppressWarnings("UnusedAssignment")
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
                    userJs.addProperty("_id", Constants.DOCUSERID);
                    userJs.addProperty("type", "user");
                    userJs.addProperty("name", Constants.DOCUSERNAME);
                    String rndPassword = new BigInteger(130, new SecureRandom()).toString(32);
                    userJs.addProperty("password", rndPassword);
                    JsonArray roles = new JsonArray();
                    roles.add(new JsonPrimitive(Constants.DOCUSERGROUP));
                    userJs.add("roles", roles);

                    try {
                        dbClient.save(userJs);
                    } catch (CouchDbException e) {
                        if (e.getMessage().equals("Conflict")) {
                            JsonObject o = dbClient.find(JsonObject.class, Constants.DOCUSERID);
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
                    dbClient = new CouchDbClient(Constants.DOCCONFIGDB, true, protocol, host, port, user, password);
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
                    p.put("dbusername", Constants.DOCUSERNAME);
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
            } catch (CouchDbException | IllegalArgumentException e) {
                throwError(resp, 1003);
            }
        }
    }

    /**
     * Sends parametrized redirect in case of an occured error.
     *
     * @param resp the http response
     * @param code error code
     * @throws IOException if an error occurred during access
     */
    private void throwError(HttpServletResponse resp, int code) throws IOException {
        resp.sendRedirect("/" + Constants.SETUP + "&e=" + code);
        resp.getWriter().println("Could not update config - Code " + code);
    }
}
