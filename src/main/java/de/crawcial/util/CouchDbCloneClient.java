package de.crawcial.util;

import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

/**
 * This class addresses a problem of LightCouch and offers a workaround to instantiate a new CouchDbClient from existing CouchDbProperties.
 *
 * @author Sebastian Lauber
 */
public class CouchDbCloneClient extends CouchDbClient {
    /**
     * This constructor works as the single parameter constructor of CouchDbClient but clones the properties object first.
     *
     * @param properties CouchDbProperties to be cloned and used
     */
    public CouchDbCloneClient(CouchDbProperties properties) {
        super(CouchDbPropertiesSource.cloneProperties(properties));
    }
}
