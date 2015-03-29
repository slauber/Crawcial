package de.crawcial.web.auth;

import de.crawcial.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet  implements the authentication API of Crawcial, that verifies the credentials and redirects the user.
 *
 * @author Sebastian Lauber
 */
public class AuthServlet extends HttpServlet {
    /**
     * This method controls the logout flow.
     * <p>request parameter: action</p>
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during validation
     * @throws IOException      if an error occurred during validation
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cookie[] reqCookies = req.getCookies();
        String redirect = Constants.LOGIN;
        if (reqCookies != null) {
            if (req.getParameter(Constants.ACTION) != null && req.getParameter(Constants.ACTION).equals("flush")) {
                for (Cookie c : reqCookies) {
                    c.setMaxAge(0);
                    resp.addCookie(c);
                }
                redirect = Constants.HOME;
            } else {
                for (Cookie c : reqCookies) {
                    if (c.getName().equals(Constants.COOKIE_NAME)) {
                        redirect = Constants.HOME;
                    }
                }
            }
        }
        resp.sendRedirect(redirect);
    }

    /**
     * This method controls the login flow.
     * <p>request parameter: action, user, password</p>
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException if an error occurred during validation
     * @throws IOException      if an error occurred during validation
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter(Constants.ACTION) != null) {
            switch (req.getParameter(Constants.ACTION)) {
                case Constants.SIGNIN:
                    String user = req.getParameter(Constants.USER);
                    String password = req.getParameter(Constants.PASSWORD);
                    Cookie c = UserServlet.verifyCredentials(getServletContext(), user, password);
                    if (c != null) {
                        resp.addCookie(c);
                        resp.sendRedirect(Constants.HOME);
                    } else {
                        resp.sendRedirect(Constants.LOGIN);
                    }
                    break;
                case Constants.SIGNOUT:
                    Cookie cookie = new Cookie(Constants.COOKIE_NAME, "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    resp.addCookie(cookie);
                    resp.sendRedirect(Constants.HOME);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            resp.getWriter().println("Your request could not be served");
        }
    }

}
