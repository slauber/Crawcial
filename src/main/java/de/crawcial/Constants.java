package de.crawcial;

/**
 * This class defines constants used in Crawcial (mainly in the web package).
 *
 * @author Sebastian Lauber
 */
@SuppressWarnings("JavaDoc")
public class Constants {
    public final static String ACTION = "action";
    public static final String CLEARCOOKIES = "auth?action=flush";
    public static final String HOME = "/";
    public static final String CONFIGDB = "crawcial_control";
    public static final String CONFIG_FILE = "config.properties";
    public static final String CONFIG_PATH = "/WEB-INF/" + CONFIG_FILE;
    public static final String SOCIAL_KEYS = "social_keys";
    public static final String TWITTER_DB = "crawcial_twitter";
    public static final String FACEBOOK_DB = "crawcial_facebook";
    public static final String SIGNIN = "signin";
    public static final String SIGNOUT = "signout";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String PRODUCTNAME = "Crawcial";
    public static final String COOKIE_NAME = "crawcialsession";
    public static final int TWITTER_ERROR = 10001;
    public static final int FACEBOOK_ERROR = 10002;
    public static final int USER_ERROR = 11000;
    public static final String USER_PREFIX = "de.crawcial.user:";
    public static final String DOCUSERID = "org.couchdb.user:crawcial_control";
    public static final String DOCUSERNAME = "crawcial_control";
    public static final String DOCUSERGROUP = DOCUSERNAME;
    public static final String DOCCONFIGDB = DOCUSERNAME;
    public static final String[] keys = {"fbappid", "fbappsecret", "fbverifytoken", "twconsumerkey", "twconsumersecret"};

    private static final String baseUrl = "?p=";
    public static final String TWITTER = baseUrl + "twitter";
    public static final String USERMGMT = baseUrl + "usermgmt";
    public static final String CONFIGURATION = baseUrl + "configuration";
    public static final String SETUP = baseUrl + "setup";
    public static final String LOGIN = baseUrl + "login";
    public static final String FACEBOOK = baseUrl + "facebook";

}
