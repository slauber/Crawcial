package de.crawcial.twitter;

import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Sebastian Lauber on 20.02.15.
 */
public class CraTwitter {

    // Properties filename
    private static final String propertiesFile = "config";

    final static private Logger logger = LoggerFactory.getLogger(CraTwitter.class);

    public static void main(String[] args) throws TwitterException, IOException {
        startAnalysis(null, true);
    }

    public static long startAnalysis(String[] overrideTerms, boolean downloadMedia) throws TwitterException, IOException {
        // Load properties from disk
        Properties properties = Utils.loadParams(propertiesFile);

        // Set terms
        List<String> terms;

        if (overrideTerms != null) {
            // Terms in config override
            terms = Arrays.asList(overrideTerms);
        } else {
            // Separate terms
            terms = Arrays.asList(properties.getProperty("terms").split("\\s*,\\s*"));
        }

        // Get timing values
        int time = Integer.valueOf(properties.getProperty("time"));
        int reps = Integer.valueOf(properties.getProperty("reps"));

        // Create new TwitterStreamer and do the auth if required
        CraTwitterStreamer craTwitterStreamer = new CraTwitterStreamer(getAuth(properties), terms, time, reps, downloadMedia);
        try {
            // return craTwitterStreamer.loadAndPersistStream(8);
            return craTwitterStreamer.loadAndPersistStream(Runtime.getRuntime().availableProcessors());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static Authentication getAuth(Properties properties) throws TwitterException, IOException {
        // Check for token & secret in properties
        if (!properties.containsKey("token") && !properties.containsKey("tokensecret")) {

            // Fire up auth in console
            Twitter twitter = TwitterFactory.getSingleton();
            twitter.setOAuthConsumer(properties.getProperty("consumerkey"), properties.getProperty("consumersecret"));
            RequestToken requestToken = twitter.getOAuthRequestToken();
            AccessToken accessToken = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (accessToken == null) {

                // Display URL and read code from console
                System.out.println("Open the following URL and grant access to your account: ");
                System.out.println(requestToken.getAuthorizationURL());
                System.out.print("Enter PIN (if available) or just hit enter. [PIN]: ");
                String pin = br.readLine();
                try {
                    if (pin.length() > 0) {
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                    } else {
                        accessToken = twitter.getOAuthAccessToken();
                    }
                } catch (TwitterException te) {
                    if (te.getStatusCode() == 401) {
                        logger.error("Unable to get the access token.");
                    } else {
                        logger.error(Arrays.toString(te.getStackTrace()));
                    }
                }
            }

            // Persist retrieved token
            properties.setProperty("token", accessToken.getToken());
            properties.setProperty("tokensecret", accessToken.getTokenSecret());
            Utils.saveParamChanges(propertiesFile, "token", accessToken.getToken());
            Utils.saveParamChanges(propertiesFile, "tokensecret", accessToken.getTokenSecret());

            logger.info("Twitter token stored");
        } else {
            logger.info("Twitter token found in properties file");
        }

        // Return authentication object
        return new OAuth1(properties.getProperty("consumerkey"), properties.getProperty("consumersecret"),
                properties.getProperty("token"), properties.getProperty("tokensecret"));
    }
}
