package de.crawcial.facebook.database;

import org.lightcouch.CouchDbProperties;

/**
 * Created by Sebastian Lauber on 18.03.2015.
 */
public class DatabaseService {
    private static DatabaseService ourInstance = new DatabaseService();
    private CouchDbProperties dbProperties;

    private DatabaseService() {
    }

    public static DatabaseService getInstance() {
        return ourInstance;
    }

    public void init(CouchDbProperties dbProperties) {
        this.dbProperties = dbProperties;
    }
}
