package de.crawcial.web;

/**
 * Created by Sebastian Lauber on 10.03.15.
 */
public class Modules {
    public static final String CLEARCOOKIES = "auth?action=flush";
    public static final String HOME = "/";
    private static final String baseUrl = "?p=";
    public static final String CHECKLOGIN = baseUrl + "auth";
    public static final String LOGIN = baseUrl + "login";
    public static final String SETUP = baseUrl + "setup";
}
