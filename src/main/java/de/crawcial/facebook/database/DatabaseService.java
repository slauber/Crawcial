package de.crawcial.facebook.database;

import org.lightcouch.CouchDbProperties;

/**
 * This singleton class handles all database activities of Crawcial for Facebook.
 * THIS IS A TODO
 *
 * @author Sebastian Lauber
 */
public class DatabaseService {
    private static DatabaseService ourInstance = new DatabaseService();

    private DatabaseService() {
    }

    /**
     * Returns the Crawcial for Facebook DatabaseService singleton.
     *
     * @return Crawcial for Facebook DatabaseService singleton
     */
    public static DatabaseService getInstance() {
        return ourInstance;
    }

    /**
     * Configures the DatabaseService singleton, must be called before the crawling process starts.
     *
     * @param dbProperties CouchDB properties for the Crawcial Facebook Database
     */
    public void init(CouchDbProperties dbProperties) {
        //noinspection unused,UnnecessaryLocalVariable
        CouchDbProperties dbProperties1 = dbProperties;
    }
}
