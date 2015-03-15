package de.crawcial;

/**
 * Created by Sebastian Lauber on 14.03.2015.
 */
public class Constants {
    public final static String ACTION = "action";
    public final static String CODE = "code";
    public static final String CLEARCOOKIES = "auth?action=flush";
    public static final String HOME = "/";
    public static final String CONFIGDB = "crawcial_control";
    public final static String CONFIG_FILE = "config.properties";
    public final static String CONFIG_PATH = "/WEB-INF/" + CONFIG_FILE;
    public final static String SOCIAL_KEYS = "social_keys";
    public final static String TWITTER_DB = "crawcial_twitter";
    public final static String FACEBOOK_DB = "crawcial_facebook";
    public final static String SIGNIN = "signin";
    public final static String SIGNOUT = "signout";
    public final static String USER = "user";
    public final static String PASSWORD = "password";
    public final static String COOKIE_NAME = "crawcialsession";
    private static final String baseUrl = "?p=";
    public final static String TWITTER = baseUrl + "twitter";
    public final static String USERMGMT = baseUrl + "usermgmt";
    public final static String CONFIGURATION = baseUrl + "configuration";
    public static final String SETUP = baseUrl + "setup";
    public static final String LOGIN = baseUrl + "login";
    public static final String FACEBOOK = baseUrl + "facebook";
}
