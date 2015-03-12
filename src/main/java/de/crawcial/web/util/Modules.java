package de.crawcial.web.util;

import org.lightcouch.CouchDbProperties;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Sebastian Lauber on 10.03.15.
 */
public class Modules {
    public static final String CLEARCOOKIES = "auth?action=flush";
    public static final String HOME = "/";
    public static final String CONFIGDB = "crawcial_control";
    public final static String CONFIG_FILE = "config.properties";
    public final static String CONFIG_PATH = "/WEB-INF/" + Modules.CONFIG_FILE;
    public final static String SOCIAL_KEYS = "social_keys";
    public final static String TWITTER_DB = "crawcial_twitter";
    public final static String FACEBOOK_DB = "crawcial_facebook";
    private static final String baseUrl = "?p=";
    public static final String LOGIN = baseUrl + "login";
    public static final String SETUP = baseUrl + "setup";
    public final static String CONFIGURATION = baseUrl + "configuration";
    public final static String USERMGMT = baseUrl + "usermgmt";
    public final static String TWITTER = baseUrl + "twitter";

    public static CouchDbProperties getCouchDbProperties(ServletContext sc, String dbName) {
        try {
            return new CouchDbProperties(dbName, false, getProperty(sc, "dbprotocol"), getProperty(sc, "dbhost"),
                    Integer.valueOf(getProperty(sc, "dbport")), getProperty(sc, "dbusername"), getProperty(sc, "dbpassword"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getProperty(ServletContext sc, String propertyName) {
        final InputStream is = sc.getResourceAsStream(CONFIG_PATH);
        Properties prop;
        try {
            prop = new Properties();
            prop.load(is);
        } catch (IOException | NullPointerException e1) {
            return null;
        }
        return prop.getProperty(propertyName);
    }
}
