package de.crawcial.web.auth;

import de.crawcial.Constants;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This class contains static helper methods to verify the session and get the session cookie.
 *
 * @author Sebastian Lauber
 */
public class AuthHelper {
    /**
     * Checks the user session by receiving the session cookie of the request and checking against the database.
     *
     * @param request the http request
     * @return true, if the session is valid
     * @throws IOException if an error occurred during validation
     */
    @SuppressWarnings("SimplifiableConditionalExpression")
    public static boolean isAuthenticated(HttpServletRequest request) throws IOException {
        return UserServlet.verifySession(request.getServletContext(), getSessionCookie(request)) ?
                true : UserServlet.isAdminParty(request.getServletContext());

    }

    /**
     * This method returns the session cookie of the request.
     *
     * @param request the http request
     * @return session cookie contents as string
     */
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
