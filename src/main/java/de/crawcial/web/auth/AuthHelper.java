package de.crawcial.web.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Sebastian Lauber on 08.03.15.
 */
public class AuthHelper {
    public static boolean isAuthenticated(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals(AuthServlet.COOKIE_NAME)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String getAllCookies(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                stringBuilder.append(c.getName());
                stringBuilder.append(": ");
                stringBuilder.append(c.getValue());
                stringBuilder.append(" - ");
                stringBuilder.append(c.getDomain());
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
