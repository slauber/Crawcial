package de.crawcial.web.social;

import de.crawcial.Constants;
import de.crawcial.facebook.FacebookStaticLoader;
import de.crawcial.facebook.FacebookStreamer;
import de.crawcial.util.CrawcialUtils;
import de.crawcial.web.auth.AuthHelper;
import de.crawcial.web.util.CrawcialWebUtils;
import de.crawcial.web.util.Tokenmanager;
import facebook4j.*;
import facebook4j.auth.AccessToken;
import facebook4j.conf.ConfigurationBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This servlet manages the Crawcial for Facebook activities.
 *
 * @author Sebastian Lauber
 */
public class FbServlet extends HttpServlet {
    static Facebook facebook;

    /**
     * Returns a list of all Facebook pages managed by the current user.
     *
     * @param req the http request
     * @return list of all Facebook pages managed by the current user
     * @throws IOException if an error occurred during access
     */
    public static ResponseList<Account> getPages(HttpServletRequest req) throws IOException {
        if (AuthHelper.isAuthenticated(req)) {
            initFacebook(req);

            try {
                AccessToken at = Tokenmanager.getFacebookAccessToken(req);
                facebook.setOAuthAccessToken(at);
                return facebook.getAccounts();
            } catch (FacebookException | IllegalStateException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Initializes the facebook4j client.
     *
     * @param req the http request
     * @return initialized facebook4j client
     */
    private static Facebook initFacebook(HttpServletRequest req) {
        if (facebook == null) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setJSONStoreEnabled(true);
            FacebookFactory ff = new FacebookFactory(cb.build());
            facebook = ff.getInstance();
        }
        try {
            facebook.setOAuthAppId(Tokenmanager.getSocialToken(req).get("fbappid"), Tokenmanager.getSocialToken(req).get("fbappsecret"));
        } catch (IllegalStateException | IOException | NullPointerException e) {
            // macht nix
        }
        return facebook;
    }

    /**
     * Invokes the one time loader for Facebook pages.
     *
     * @param req    the http request
     * @param pageId the page id to be loaded
     * @throws FacebookException if the Facebook connection fails
     */
    public void callStaticLoader(HttpServletRequest req, String pageId) throws FacebookException {
        FacebookStaticLoader.getInstance().setFbVars(facebook, Tokenmanager.getFacebookAccessToken(req));
        FacebookStaticLoader.getInstance().downloadPage(pageId, CrawcialWebUtils.getCouchDbProperties(getServletContext(), Constants.FACEBOOK_DB));
    }

    /**
     * Enables Crawcial on Facebook pages and handles Facebook Callback verifications.
     * <p>request parameter: action, pageid, hub.mode, hub.challenge, hub.verify_token</p>
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter(Constants.ACTION) != null && AuthHelper.isAuthenticated(req)) {
            initFacebook(req);
            facebook.setOAuthAccessToken(Tokenmanager.getFacebookAccessToken(req));
            if (req.getParameter(Constants.ACTION).equals("enablePage") && req.getParameter("pageid") != null) {
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
        }

        // Respond to activation from Facebook
        if (req.getParameter("hub.mode") != null && req.getParameter("hub.challenge") != null && req.getParameter("hub.verify_token") != null) {
            if (req.getParameter("hub.mode").equals("subscribe") && req.getParameter("hub.verify_token").
                    equalsIgnoreCase(Tokenmanager.getSocialToken(req).get("fbverifytoken"))) {
                resp.getWriter().print(req.getParameter("hub.challenge"));
            }
        }

    }

    /**
     * Enables Callbacks for Crawcial as a Facebook consumer, loads page contents on demand and handles Facebook Callbacks.
     * <p>request parameter: action, callback, pageid</p>
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> socialToken = Tokenmanager.getSocialToken(req);
        if (req.getParameter(Constants.ACTION) != null && AuthHelper.isAuthenticated(req)) {
            initFacebook(req);
            facebook.setOAuthAccessToken(new AccessToken(socialToken.get("fbappid") + "|" +
                    socialToken.get("fbappsecret")));
            switch (req.getParameter(Constants.ACTION)) {
                case "setupSubscriptions":
                    HashMap<String, String> params = new HashMap<>();
                    try {
                        params.put("callback_url", req.getParameter("callback"));
                        params.put("fields", "feed");
                        params.put("verify_token", socialToken.get("fbverifytoken"));
                        params.put("object", "page");
                        facebook.rawAPI().callPostAPI(socialToken.get("fbappid") + "/subscriptions", params);
                        resp.getWriter().println("App successfully prepared for Crawcial");
                    } catch (FacebookException e) {
                        resp.getWriter().println("An error occured");
                        resp.getWriter().println(e.getErrorMessage());
                    }
                    break;
                case "staticLoader":
                    if (req.getParameter("pageid") != null) {
                        try {
                            callStaticLoader(req, req.getParameter("pageid"));
                            resp.getWriter().println("Started downloading content of Facebook Page ID: " + req.getParameter("pageid"));
                        } catch (FacebookException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        } else {
            // 1. get received JSON data from request
            String signature = req.getHeader("X-Hub-Signature");
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
            String json = br.readLine();
            if (CrawcialUtils.verifyFbSignature(json, signature, socialToken.get("fbappsecret"))) {
                resp.setStatus(200);
                log(json);
                FacebookStreamer.setFbVars(initFacebook(req), Tokenmanager.getFacebookAccessToken(req));
                FacebookStreamer.parseChange(json, CrawcialWebUtils.getCouchDbProperties(getServletContext(), Constants.FACEBOOK_DB));
            } else {
                resp.setStatus(403);
            }
        }
    }
}
