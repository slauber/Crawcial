package de.crawcial.web.auth;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

public class TwAuth extends HttpServlet {

    private final static String CONFIG_FILE = "social.properties";
    private final static String CONFIG_PATH = "/WEB-INF/" + CONFIG_FILE;

    private String CONSUMERKEY;
    private String CONSUMERSECRET;

    private void loadProperties() {
        final InputStream is = getServletContext().getResourceAsStream(CONFIG_PATH);
        try {
            Properties prop = new Properties();
            prop.load(is);
            CONSUMERKEY = prop.getProperty("twconsumerkey");
            CONSUMERSECRET = prop.getProperty("twconsumersecret");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Twitter twitter = new TwitterFactory().getInstance();
        loadProperties();
        twitter.setOAuthConsumer(CONSUMERKEY, CONSUMERSECRET);
        req.getSession().setAttribute("twitter", twitter);
        try {
            StringBuilder callbackURL = new StringBuilder(req.getRequestURL());
            int index = callbackURL.lastIndexOf("/");
            callbackURL.replace(index, callbackURL.length(), "").append("/twauth");

            RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL.toString());
            req.getSession().setAttribute("requestToken", requestToken);
            resp.sendRedirect(requestToken.getAuthenticationURL());

        } catch (TwitterException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Twitter twitter = (Twitter) req.getSession().getAttribute("twitter");
        RequestToken requestToken = (RequestToken) req.getSession().getAttribute("requestToken");
        String verifier = req.getParameter("oauth_verifier");
        try {
            AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
            req.getSession().removeAttribute("requestToken");
            Cookie tokenCookie = new Cookie("twtoken", Base64.getEncoder().encodeToString(
                    (token.getToken() + " - " + token.getTokenSecret()).getBytes()));
            tokenCookie.setMaxAge(-1);
            tokenCookie.setHttpOnly(true);
            resp.addCookie(tokenCookie);
            resp.sendRedirect("/");
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
    }


}