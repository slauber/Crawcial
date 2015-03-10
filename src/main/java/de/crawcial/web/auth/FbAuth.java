package de.crawcial.web.auth;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Properties;

public class FbAuth extends HttpServlet {

    private final static String CONFIG_FILE = "social.properties";
    private final static String CONFIG_PATH = "/WEB-INF/" + CONFIG_FILE;
    public static String FB_APP_ID;
    public static String FB_APP_SECRET;
    public static String REDIRECT_URI;
    static String accessToken = "";

    private void loadProperties(String reqUri) {
        final InputStream is = getServletContext().getResourceAsStream(CONFIG_PATH);
        try {
            StringBuilder callbackURL = new StringBuilder(reqUri);
            int index = callbackURL.lastIndexOf("/");
            callbackURL.replace(index, callbackURL.length(), "").append("/fbauth");

            Properties prop = new Properties();
            prop.load(is);
            FB_APP_ID = prop.getProperty("fbappid");
            FB_APP_SECRET = prop.getProperty("fbappsecret");
            REDIRECT_URI = callbackURL.toString();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        loadProperties(req.getRequestURL().toString());
        resp.sendRedirect(getFbAuthUrl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        loadProperties(req.getRequestURL().toString());
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
            resp.sendRedirect("/");
        }
    }

    public String getFbAuthUrl() {
        String fbLoginUrl = "";
        try {
            fbLoginUrl = "http://www.facebook.com/dialog/oauth?" + "client_id="
                    + FB_APP_ID + "&redirect_uri="
                    + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                    + "&scope=email";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fbLoginUrl;
    }

    public String getFbGraphUrl(String code) {
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

    private String getAccessToken(String code) {
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
