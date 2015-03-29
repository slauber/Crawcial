package de.crawcial.util;

import org.lightcouch.CouchDbProperties;

import java.io.IOException;
import java.util.Properties;

/**
 * This utility class contains a CouchDbProperty loader from files and a clone method for CouchDbProperties.
 *
 * @author Sebastian Lauber
 */
public class CouchDbPropertiesSource {
    /**
     * Generates a CouchDbProperties object from a config file.
     * <p>Request parameter: dbname, createdbifnotexist, protocol, port, host, username, password</p>
     *
     * @param filename the file to read the configuration from
     * @return a generated CouchDbProperties object
     * @throws IOException              if the file cannot be accessed
     * @throws IllegalArgumentException if the file does not contain all required parameters
     */
    public static CouchDbProperties loadFromFile(String filename) throws IOException, IllegalArgumentException {
        Properties defaultProperties = new Properties();
        CouchDbProperties properties;
        defaultProperties.load(CouchDbPropertiesSource.class.getClassLoader().getResourceAsStream(filename));
        // Load mandatory fields if available

        if (defaultProperties.containsKey("dbname") && defaultProperties.containsKey("createdbifnotexist")
                && defaultProperties.containsKey("protocol") && defaultProperties.containsKey("host") &&
                defaultProperties.containsKey("port") && defaultProperties.containsKey("username") &&
                defaultProperties.containsKey("password")) {
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

    /**
     * This works around an issue with the LightCouch client, CouchDbProperties are mutable and cannot be reused safely.
     *
     * @param src CouchDbProperties to be cloned
     * @return cloned CouchDbProperties
     */
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
