package de.crawcial.web.auth;

import de.crawcial.Constants;
import de.crawcial.web.util.Tokenmanager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;

/**
 * This servlet handles the Facebook login process.
 *
 * @author Sebastian Lauber
 */
public class FbAuth extends HttpServlet {
    private static String FB_APP_ID;
    private static String FB_APP_SECRET;
    private static String REDIRECT_URI;
    private static String accessToken = "";

    /**
     * Loads Facebook OAuth tokens from the database.
     *
     * @param req the http request
     * @throws IOException if an error occurred during access
     */
    private void loadProperties(HttpServletRequest req) throws IOException {
        StringBuilder callbackURL = new StringBuilder(req.getRequestURL().toString());
        int index = callbackURL.lastIndexOf("/");
        callbackURL.replace(index, callbackURL.length(), "").append("/fbauth");
        Map<String, String> socialToken = Tokenmanager.getSocialToken(req);
        FB_APP_ID = socialToken.get("fbappid");
        FB_APP_SECRET = socialToken.get("fbappsecret");
        REDIRECT_URI = callbackURL.toString();
    }

    /**
     * This method handles the Facebook OAuth callback and sets the fbtoken cookie.
     * <p>request parameter: code</p>
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        loadProperties(req);
        String code = req.getParameter("code");
        if (code == null || code.equals("")) {
            throw new RuntimeException(
                    "ERROR: Didn't get code parameter in callback.");
        } else {
            String token = getAccessToken(code);
            Cookie tokenCookie = new Cookie("fbtoken", Base64.getEncoder().encodeToString(token.getBytes()));
            tokenCookie.setMaxAge(-1);
            tokenCookie.setHttpOnly(true);
            resp.addCookie(tokenCookie);
            resp.sendRedirect(Constants.FACEBOOK);
        }
    }

    /**
     * This method checks the configuration status and redirects to the configuration page, if necessary.
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        loadProperties(req);
        if (FB_APP_ID == null || FB_APP_ID.equals("") || FB_APP_SECRET == null || FB_APP_SECRET.equals("")) {
            resp.sendRedirect(Constants.CONFIGURATION);
        } else {
            resp.sendRedirect(getFbAuthUrl());
        }
    }

    /**
     * Returns the Facebook authentication URL.
     *
     * @return Facebook authentication url string
     */
    private String getFbAuthUrl() {
        String fbLoginUrl = "";
        try {
            fbLoginUrl = "http://www.facebook.com/dialog/oauth?" + "client_id="
                    + FB_APP_ID + "&redirect_uri="
                    + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                    + "&scope=email,manage_pages";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fbLoginUrl;
    }

    /**
     * Returns the Facebook Graph API url string in order to exchange the code for a token.
     *
     * @param code OAuth authentication code
     * @return Facebook Graph API URL string for code/token exchange
     */
    private String getFbGraphUrl(String code) {
        String fbGraphUrl = "";
        try {
            fbGraphUrl = "https://graph.facebook.com/oauth/access_token?"
                    + "client_id=" + FbAuth.FB_APP_ID + "&redirect_uri="
                    + URLEncoder.encode(FbAuth.REDIRECT_URI, "UTF-8")
                    + "&client_secret=" + FB_APP_SECRET + "&code=" + code;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fbGraphUrl;
    }

    /**
     * Returns an Facebook OAuth2 access token.
     *
     * @param code the callback code
     * @return Facebook OAuth2 access token string
     * @throws RuntimeException if connection with Facebook throws errors
     */
    private String getAccessToken(String code) throws RuntimeException {
        if ("".equals(accessToken)) {
            URL fbGraphURL;
            try {
                fbGraphURL = new URL(getFbGraphUrl(code));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new RuntimeException("Invalid code received " + e);
            }
            URLConnection fbConnection;
            StringBuffer b;
            try {
                fbConnection = fbGraphURL.openConnection();
                BufferedReader in;
                in = new BufferedReader(new InputStreamReader(
                        fbConnection.getInputStream()));
                String inputLine;
                b = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    b.append(inputLine);
                    b.append("\n");
                }

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to connect with Facebook "
                        + e);
            }

            accessToken = b.toString();
            if (accessToken.startsWith("{")) {
                throw new RuntimeException("ERROR: Access Token Invalid: "
                        + accessToken);
            }
        }
        return accessToken;
    }
}
