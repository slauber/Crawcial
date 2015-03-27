package de.crawcial.web.setup;

import de.crawcial.Constants;
import de.crawcial.web.util.CrawcialWebUtils;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class provides information about the configuration state.
 *
 * @author Sebastian Lauber
 */
public class Validator {

    // Internal status codes for validation
    public final static int OK = 0;
    public final static int NO_DATABASE_CONNECTION = 1;
    public final static int NO_CONFIG_FILE = 2;
    public final static int NO_VALID_CONFIG = 3;


    /**
     * Returns information about the configuration state, checks configuration file.
     *
     * @param sc the servlet context
     * @return status code of database connection
     */
    public static int isDbConfigured(ServletContext sc) {
        final InputStream is = sc.getResourceAsStream(Constants.CONFIG_PATH);
        if (is == null) {
            return NO_CONFIG_FILE;
        }
        try {
            Properties prop = new Properties();
            prop.load(is);
            final boolean createdb = false;
            final String dbprotocol = prop.getProperty("dbprotocol");
            final String dbhost = prop.getProperty("dbhost");
            final int dbport = Integer.valueOf(prop.getProperty("dbport"));
            final String dbusername = prop.getProperty("dbusername");
            final String dbpassword = prop.getProperty("dbpassword");


            // Check for mandatory variables
            if (dbprotocol == null || dbhost == null || dbusername == null || dbpassword == null) {
                return NO_VALID_CONFIG;
            }

            // Try to establish a database connection to verify credentials
            CouchDbClient c = new CouchDbClient(Constants.CONFIGDB, createdb, dbprotocol, dbhost, dbport, dbusername, dbpassword);
            is.close();
            c.shutdown();
            return OK;
        } catch (IOException e) {
            return NO_CONFIG_FILE;
        } catch (NumberFormatException e) {
            return NO_VALID_CONFIG;
        } catch (CouchDbException e) {
            return NO_DATABASE_CONNECTION;
        }
    }

    /**
     * Returns whether displaying setup wizard is allowed.
     *
     * @param sc the servlet context
     * @return true, if displaying setup wizard is allowed
     */
    public static boolean isSetupEnabled(ServletContext sc) {
        int status = isDbConfigured(sc);

        // If no (valid) config exists, a setup wizard will be displayed
        if (status == NO_VALID_CONFIG || status == NO_CONFIG_FILE) {
            return true;
        }

        // Same applies to having no database connection but this checks for
        // a flag in our config, whether this has been allowed
        if (status == NO_DATABASE_CONNECTION) {
            try {
                String rwProperty = CrawcialWebUtils.getProperty(sc, "rewriteProperty");
                return Boolean.valueOf(rwProperty);
            } catch (NullPointerException e) {
                return false;
            }
        }
        return false;
    }
}
