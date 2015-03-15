package de.crawcial.web.social;

import de.crawcial.Constants;
import de.crawcial.web.auth.AuthHelper;
import de.crawcial.web.util.Tokenmanager;
import facebook4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Sebastian Lauber on 14.03.2015.
 */
public class FbServlet extends HttpServlet {
    final static private Logger logger = LoggerFactory.getLogger(FbServlet.class);
    static Facebook facebook = FacebookFactory.getSingleton();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter(Constants.ACTION) != null && AuthHelper.isAuthenticated(req)) {
            try {
                facebook.setOAuthAppId(Tokenmanager.getSocialToken(req).get("fbappid"), Tokenmanager.getSocialToken(req).get("fbappsecret"));
                facebook.setOAuthAccessToken(Tokenmanager.getFacebookAccessToken(req));
            } catch (IllegalStateException e) {
                // macht nix
            }
            switch (req.getParameter(Constants.ACTION)) {
                case "getPages":
                    ResponseList<Account> accounts = null;
                    try {
                        accounts = facebook.getAccounts();
                        for (Account a : accounts) {
                            if (a.getPerms().contains("ADMINISTER")) {
                                resp.getWriter().print("<a href=\"https://facebook.com/" + a.getId() + "\">");
                                resp.getWriter().println(a.getId() + " - " + a.getName() + " " + a.getAccessToken());
                                resp.getWriter().println(a.getPerms() + "</a><br>\n");
                            }
                        }
                    } catch (FacebookException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }

        // Respond to activation from Facebook
        if (req.getParameter("hub.mode") != null && req.getParameter("hub.challenge") != null && req.getParameter("hub.verify_token") != null) {
            if (req.getParameter("hub.mode").equals("subscribe")) {
                resp.getWriter().print(req.getParameter("hub.challenge"));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. get received JSON data from request
        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String json = br.readLine();
        logger.info(json);
        resp.setStatus(200);
    }
}
