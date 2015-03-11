package de.crawcial.web.social;

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
import java.util.Arrays;
import java.util.List;

/**
 * Created by Sebastian Lauber on 11.03.2015.
 */
public class TwitterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (AuthHelper.isAuthenticated(req) && req.getParameter("action") != null && req.getParameter("action").equals("persist")) {
            CouchDbProperties dbProperties = Modules.getCouchDbProperties(req.getServletContext(), Modules.TWITTER_DB);
            int duration = req.getParameter("duration") == null ? -1 : Integer.valueOf(req.getParameter("duration"));
            List<String> terms = req.getParameter("terms") == null ? null : Arrays.asList(req.getParameter("terms").split("\\s*,\\s*"));
            if (dbProperties != null && duration != -1 && terms != null) {
                CraTwitterStreamer crs = CraTwitterStreamer.getInstance();
                crs.setConfig(Tokenmanager.getTwitterOAuth(req), terms, duration, true, dbProperties);
                Thread t = new Thread(crs);
                t.setName("Master-of-desaster");
                t.start();
                try {
                    t.join();
                    resp.getWriter().println(crs.getResult());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
