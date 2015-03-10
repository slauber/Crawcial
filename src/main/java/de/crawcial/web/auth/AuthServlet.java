package de.crawcial.web.auth;

import de.crawcial.web.Modules;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Sebastian Lauber on 08.03.15.
 */
public class AuthServlet extends HttpServlet {
    public final static String SIGNIN = "signin";
    public final static String SIGNOUT = "signout";
    public final static String ACTION = "action";
    public final static String USER = "user";
    public final static String PASSWORD = "password";
    public final static String COOKIE_NAME = "crawcialsession";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter(ACTION) != null) {
            switch (req.getParameter(ACTION)) {
                case SIGNIN:
                    String user = req.getParameter(USER);
                    String password = req.getParameter(PASSWORD);
                    if (user != null && password != null && user.equals(password)) {
                        String hash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(password + "salt" + String.valueOf(Math.random()));
                        Cookie cookie = new Cookie(COOKIE_NAME, hash);
                        cookie.setHttpOnly(true);
                        cookie.setPath("/");
                        resp.addCookie(cookie);
                        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                        resp.setHeader("Location", Modules.HOME);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                        resp.setHeader("Location", Modules.LOGIN);
                    }
                    break;
                case SIGNOUT:
                    Cookie cookie = new Cookie(COOKIE_NAME, "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    resp.addCookie(cookie);
                    resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    resp.setHeader("Location", Modules.HOME);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            resp.getWriter().println("Your request could not be served");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cookie[] reqCookies = req.getCookies();
        if (reqCookies != null) {
            if (req.getParameter("action") != null && req.getParameter("action").equals("flush")) {
                for (Cookie c : reqCookies) {
                    c.setMaxAge(0);
                    resp.addCookie(c);
                }
                resp.sendRedirect(Modules.HOME);
            } else {
                String redirect = Modules.LOGIN;
                for (Cookie c : reqCookies) {
                    if (c.getName().equals(COOKIE_NAME)) {
                        redirect = Modules.HOME;
                    }
                }
                resp.sendRedirect(redirect);
            }
        }
    }

}
