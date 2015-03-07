package de.crawcial.database.util;

import org.lightcouch.CouchDbProperties;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Sebastian Lauber on 07.03.2015.
 */
public class CouchDBPropertiesSource {
    public static CouchDbProperties loadFromFile(String filename) throws IOException, IllegalArgumentException {
        Properties defaultProperties = new Properties();
        CouchDbProperties properties;
        defaultProperties.load(CouchDBPropertiesSource.class.getClassLoader().getResourceAsStream(filename));
        // Load mandatory fields if available

        if (defaultProperties.containsKey("dbname") && defaultProperties.containsKey("createdbifnotexist")
                && defaultProperties.containsKey("protocol") && defaultProperties.containsKey("host") &&
                defaultProperties.containsKey("port") && defaultProperties.containsKey("username")) {
            properties = new CouchDbProperties(defaultProperties.getProperty("dbname"),
                    Boolean.valueOf(defaultProperties.getProperty("createdbifnotexist")),
                    defaultProperties.getProperty("protocol"),
                    defaultProperties.getProperty("host"),
                    Integer.valueOf(defaultProperties.getProperty("port")),
                    defaultProperties.getProperty("username"),
                    defaultProperties.getProperty("password"));

            if (defaultProperties.containsKey("path")) {
                properties.setPath(defaultProperties.getProperty("path"));
            }
            // Load optional fields
            if (defaultProperties.containsKey("sockettimeout")) {
                properties.setSocketTimeout(Integer.valueOf(defaultProperties.getProperty("sockettimeout")));
            }
            if (defaultProperties.containsKey("connectiontimeout")) {
                properties.setConnectionTimeout(Integer.valueOf(defaultProperties.getProperty("connectiontimeout")));
            }
            if (defaultProperties.containsKey("maxconnections")) {
                properties.setMaxConnections(Integer.valueOf(defaultProperties.getProperty("maxconnections")));
            }
            if (defaultProperties.containsKey("proxyhost")) {
                properties.setProxyHost(defaultProperties.getProperty("proxyhost"));
            }
            if (defaultProperties.containsKey("proxyport")) {
                properties.setProxyPort(Integer.valueOf(defaultProperties.getProperty("proxyport")));
            }
            return properties;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static CouchDbProperties cloneProperties(CouchDbProperties src) {
        // (String dbName, boolean createDbIfNotExist, String protocol, String host, int port, String username, String password)
        CouchDbProperties ret = new CouchDbProperties(src.getDbName(), src.isCreateDbIfNotExist(), src.getProtocol(), src.getHost(), src.getPort(), src.getUsername(), src.getPassword());
        ret.setPath(src.getPath());
        ret.setSocketTimeout(src.getSocketTimeout());
        ret.setConnectionTimeout(src.getConnectionTimeout());
        ret.setMaxConnections(src.getMaxConnections());
        ret.setProxyHost(src.getProxyHost());
        ret.setProxyPort(src.getProxyPort());
        return ret;
    }
}
