package de.crawcial.web.social;

import com.twitter.hbc.httpclient.auth.OAuth1;
import de.crawcial.twitter.CraTwitterStreamer;
import de.crawcial.web.auth.AuthHelper;
import de.crawcial.web.util.Modules;
import de.crawcial.web.util.Tokenmanager;
import org.lightcouch.CouchDbProperties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Sebastian Lauber on 11.03.2015.
 */
public class TwitterServlet extends HttpServlet {

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthHelper.isAuthenticated(req) && req.getParameter("action") != null) {
            CraTwitterStreamer crs = CraTwitterStreamer.getInstance();
            switch (req.getParameter("action")) {
                case "persist":
                    CouchDbProperties dbProperties = Modules.getCouchDbProperties(req.getServletContext(), Modules.TWITTER_DB);
                    List<String> terms = req.getParameter("terms") == null ? null : Arrays.asList(req.getParameter("terms").split("\\s*,\\s*"));
                    OAuth1 oauth = null;
                    try {
                        oauth = Tokenmanager.getTwitterOAuth(req);
                    } catch (URISyntaxException e) {
                    }
                    if (oauth != null && dbProperties != null && terms != null) {
                        crs.setConfig(oauth, terms, true, dbProperties);
                        Thread t = new Thread(crs);
                        t.setName("Master-of-desaster");
                        t.start();
                    }
                    break;
                case "shutdown":
                    crs.shutdown();
                    break;
                default:
                    break;
            }
            resp.sendRedirect(Modules.TWITTER);
        }
    }
}
