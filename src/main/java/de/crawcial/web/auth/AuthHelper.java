package de.crawcial.web.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Sebastian Lauber on 08.03.15.
 */
public class AuthHelper {
    public static boolean isAuthenticated(HttpServletRequest request) throws IOException {
        return UserServlet.verifySession(request.getServletContext(), getSessionCookie(request)) ?
                true : UserServlet.isAdminParty(request.getServletContext());

    }

    public static String getSessionCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals(AuthServlet.COOKIE_NAME)) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
