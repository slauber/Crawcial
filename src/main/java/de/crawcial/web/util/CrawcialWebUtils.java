package de.crawcial.web.util;

import de.crawcial.Constants;
import org.lightcouch.CouchDbProperties;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class that receives the database configuration from disk.
 *
 * @author Sebastian Lauber
 */
public class CrawcialWebUtils {
    /**
     * Returns the current database properties from a properties file with a specified database name.
     *
     * @param sc     the servlet context
     * @param dbName the database name
     * @return current database properties with a specified database name
     */
    @SuppressWarnings("ConstantConditions")
    public static CouchDbProperties getCouchDbProperties(ServletContext sc, String dbName) {
        try {
            return new CouchDbProperties(dbName, false, getProperty(sc, "dbprotocol"), getProperty(sc, "dbhost"),
                    Integer.valueOf(getProperty(sc, "dbport")), getProperty(sc, "dbusername"), getProperty(sc, "dbpassword"));
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Utility method to receive properties from disk.
     *
     * @param sc           the servlet context
     * @param propertyName the requested property's name
     * @return requested property as string
     */
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
