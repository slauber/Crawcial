package de.crawcial.web.social;

import de.crawcial.Constants;
import de.crawcial.web.auth.AuthHelper;
import de.crawcial.web.util.Tokenmanager;
import facebook4j.*;
import facebook4j.auth.AccessToken;
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

    public static ResponseList<Account> getPages(HttpServletRequest req) throws IOException {
        if (AuthHelper.isAuthenticated(req)) {
            try {
                facebook.setOAuthAppId(Tokenmanager.getSocialToken(req).get("fbappid"), Tokenmanager.getSocialToken(req).get("fbappsecret"));
            } catch (IllegalStateException e) {
                // Macht nix
            } catch (IOException e) {
                return null;
            }
            facebook.setOAuthAccessToken(Tokenmanager.getFacebookAccessToken(req));
            try {
                return facebook.getAccounts();
            } catch (FacebookException | IllegalStateException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter(Constants.ACTION) != null && AuthHelper.isAuthenticated(req)) {
            try {
                facebook.setOAuthAppId(Tokenmanager.getSocialToken(req).get("fbappid"), Tokenmanager.getSocialToken(req).get("fbappsecret"));
            } catch (IllegalStateException e) {
                // macht nix
            }
            facebook.setOAuthAccessToken(Tokenmanager.getFacebookAccessToken(req));
            switch (req.getParameter(Constants.ACTION)) {
                case "enablePage":
                    if (req.getParameter("pageid") != null) {
                        try {
                            ResponseList<Account> accounts = facebook.getAccounts();
                            AccessToken at = null;
                            for (Account a : accounts) {
                                if (a.getPerms().contains("ADMINISTER") && a.getId().equalsIgnoreCase(req.getParameter("pageid"))) {
                                    at = new AccessToken(a.getAccessToken());
                                }
                            }
                            if (at != null) {
                                facebook.setOAuthAccessToken(at);
                                facebook.pages().installTab(req.getParameter("pageid"), Tokenmanager.getSocialToken(req).get("fbappid"));
                                facebook.rawAPI().callPostAPI(req.getParameter("pageid") + "/subscribed_apps");
                                resp.getWriter().println("Crawcial was successfully installed on Page ID: " + req.getParameter("pageid"));
                            }
                        } catch (FacebookException e) {
                            e.printStackTrace();
                        }
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
