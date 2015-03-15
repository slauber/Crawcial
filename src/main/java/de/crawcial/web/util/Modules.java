package de.crawcial.web.util;

import de.crawcial.Constants;
import org.lightcouch.CouchDbProperties;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Sebastian Lauber on 10.03.15.
 */
public class Modules {
    public static CouchDbProperties getCouchDbProperties(ServletContext sc, String dbName) {
        try {
            return new CouchDbProperties(dbName, false, getProperty(sc, "dbprotocol"), getProperty(sc, "dbhost"),
                    Integer.valueOf(getProperty(sc, "dbport")), getProperty(sc, "dbusername"), getProperty(sc, "dbpassword"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getProperty(ServletContext sc, String propertyName) {
        final InputStream is = sc.getResourceAsStream(Constants.CONFIG_PATH);
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
