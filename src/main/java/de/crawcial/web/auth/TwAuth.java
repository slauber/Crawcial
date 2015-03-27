package de.crawcial.web.auth;

import de.crawcial.Constants;
import de.crawcial.web.util.Tokenmanager;
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
import java.util.Base64;
import java.util.Map;

/**
 * This servlet handles the Twitter login process.
 *
 * @author Sebastian Lauber
 */
public class TwAuth extends HttpServlet {
    private String CONSUMERKEY;
    private String CONSUMERSECRET;

    private void loadProperties(HttpServletRequest req) throws IOException {
        Map<String, String> socialToken = Tokenmanager.getSocialToken(req);
        CONSUMERKEY = socialToken.get("twconsumerkey");
        CONSUMERSECRET = socialToken.get("twconsumersecret");
    }


    /**
     * This method checks for the required configuration and controls the Twitter login process.
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        loadProperties(req);
        if (CONSUMERKEY == null || CONSUMERKEY.equals("") || CONSUMERSECRET == null || CONSUMERSECRET.equals("")) {
            resp.sendRedirect(Constants.CONFIGURATION + "&e=" + Constants.TWITTER_ERROR);
        } else {
            Twitter twitter = new TwitterFactory().getInstance();
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
                resp.sendRedirect(Constants.CONFIGURATION + "&e=" + Constants.TWITTER_ERROR);
            }
        }
    }

    /**
     * This method handles the Twitter OAuth callback and sets the twtoken cookie.
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during access
     * @throws IOException      if an error occurred during access
     */
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
            resp.sendRedirect(Constants.TWITTER);
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
    }
}