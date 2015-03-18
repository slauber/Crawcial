package de.crawcial.util;

import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

/**
 * Created by Sebastian Lauber on 07.03.2015.
 */
public class CouchDbCloneClient extends CouchDbClient {
    public CouchDbCloneClient(CouchDbProperties properties) {
        super(CouchDBPropertiesSource.cloneProperties(properties));
    }
}
