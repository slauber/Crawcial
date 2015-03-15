package de.crawcial.web.auth;

import de.crawcial.Constants;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Sebastian Lauber on 08.03.15.
 */
public class AuthHelper {
    @SuppressWarnings("SimplifiableConditionalExpression")
    public static boolean isAuthenticated(HttpServletRequest request) throws IOException {
        return UserServlet.verifySession(request.getServletContext(), getSessionCookie(request)) ?
                true : UserServlet.isAdminParty(request.getServletContext());

    }

    public static String getSessionCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals(Constants.COOKIE_NAME)) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
