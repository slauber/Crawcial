package de.crawcial.twitter;

import java.io.*;
import java.util.Properties;

/**
 * Created by Sebastian Lauber on 21.02.15.
 */
class Utils {
    public static Properties loadParams(String name) {
        Properties props = new Properties();
        InputStream is;

        // First try loading from the current directory
        try {
            File f = new File(name + ".properties");
            is = new FileInputStream(f);
        } catch (Exception e) {
            is = null;
        }

        try {
            if (is == null) {
                // Try loading from classpath
                is = Utils.class.getClassLoader().getResourceAsStream(name + ".properties");
            }
            props.load(is);
        } catch (Exception e) {
            return null;
        }
        return props;
    }

    public static void saveParamChanges(String name, String property, String value) {
        try {
            Properties props = loadParams(name);
            if (props == null) {
                props = new Properties();
            }
            props.setProperty(property, value);
            File f = new File(name + ".properties");
            OutputStream out = new FileOutputStream(f);
            props.store(out, name + " properties file - required by Crawcial");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
