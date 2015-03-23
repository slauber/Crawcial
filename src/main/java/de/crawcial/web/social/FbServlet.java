package de.crawcial.web.social;

import de.crawcial.Constants;
import de.crawcial.facebook.FacebookStaticLoader;
import de.crawcial.facebook.FacebookStreamer;
import de.crawcial.web.auth.AuthHelper;
import de.crawcial.web.util.Modules;
import de.crawcial.web.util.Tokenmanager;
import facebook4j.*;
import facebook4j.auth.AccessToken;
import facebook4j.conf.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sebastian Lauber on 14.03.2015.
 */
public class FbServlet extends HttpServlet {
    final static private Logger logger = LoggerFactory.getLogger(FbServlet.class);
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    static Facebook facebook;

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

    public static boolean verifySignature(String payload, String signature, String secret) {
        boolean isValid = false;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(payload.getBytes());

            String expected = signature.substring(5);
            String actual = new String(encode(rawHmac));

            isValid = expected.equals(actual);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException e) {
            logger.warn(e.getLocalizedMessage());
        }

        return isValid;
    }

    private static char[] encode(byte[] bytes) {
        final int amount = bytes.length;
        char[] result = new char[2 * amount];

        int j = 0;
        for (byte aByte : bytes) {
            result[j++] = HEX[(0xF0 & aByte) >>> 4];
            result[j++] = HEX[(0x0F & aByte)];
        }

        return result;
    }

    private static Facebook initFacebook(HttpServletRequest req) {
        if (facebook == null) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setJSONStoreEnabled(true);
            FacebookFactory ff = new FacebookFactory(cb.build());
            facebook = ff.getInstance();
        }
        try {
            facebook.setOAuthAppId(Tokenmanager.getSocialToken(req).get("fbappid"), Tokenmanager.getSocialToken(req).get("fbappsecret"));
        } catch (IllegalStateException | IOException e) {
            // macht nix
        }
        return facebook;
    }

    public void callStaticLoader(HttpServletRequest req, String pageId) throws FacebookException {
        FacebookStaticLoader.getInstance().setFbVars(facebook, Tokenmanager.getFacebookAccessToken(req));
        FacebookStaticLoader.getInstance().downloadPage(pageId, Modules.getCouchDbProperties(getServletContext(), Constants.FACEBOOK_DB));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter(Constants.ACTION) != null && AuthHelper.isAuthenticated(req)) {
            initFacebook(req);
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
            if (req.getParameter("hub.mode").equals("subscribe") && req.getParameter("hub.verify_token").
                    equalsIgnoreCase(Tokenmanager.getSocialToken(req).get("fbverifytoken"))) {
                resp.getWriter().print(req.getParameter("hub.challenge"));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> socialToken = Tokenmanager.getSocialToken(req);
        if (req.getParameter(Constants.ACTION) != null && AuthHelper.isAuthenticated(req)) {
            initFacebook(req);
            facebook.setOAuthAccessToken(new AccessToken(socialToken.get("fbappid") + "|" +
                    socialToken.get("fbappsecret")));
            switch (req.getParameter("action")) {
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
            if (verifySignature(json, signature, socialToken.get("fbappsecret"))) {
                resp.setStatus(200);
                log(json);
                FacebookStreamer.setFbVars(initFacebook(req), Tokenmanager.getFacebookAccessToken(req));
                FacebookStreamer.parseChange(json, Modules.getCouchDbProperties(getServletContext(), Constants.FACEBOOK_DB));
            } else {
                resp.setStatus(403);
            }
        }
    }
}
