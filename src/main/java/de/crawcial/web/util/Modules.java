package de.crawcial.web.util;

import org.lightcouch.CouchDbClient;

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
    private static final String baseUrl = "?p=";
    public static final String LOGIN = baseUrl + "login";
    public static final String SETUP = baseUrl + "setup";
    public static final String CONFIGDB = "crawcial_control";
    public final static String CONFIG_FILE = "config.properties";
    public final static String CONFIG_PATH = "/WEB-INF/" + Modules.CONFIG_FILE;
    public final static String SOCIAL_KEYS = "social_keys";
    public final static String DASHBOARD_CONFIG = baseUrl + "dashboard";

    public static final CouchDbClient getDbClient(ServletContext sc, String database) throws IOException {

        return new CouchDbClient(database, false, getProperty(sc, "dbprotocol"), getProperty(sc, "dbhost"),
                Integer.valueOf(getProperty(sc, "dbport")), getProperty(sc, "dbusername"), getProperty(sc, "dbpassword"));
    }

    public static String getProperty(ServletContext sc, String propertyName) throws IOException {
        final InputStream is = sc.getResourceAsStream(CONFIG_PATH);
        Properties prop = new Properties();
        prop.load(is);
        return prop.getProperty(propertyName);
    }
}
