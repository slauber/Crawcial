package de.crawcial.web.auth;

import com.google.gson.JsonObject;
import de.crawcial.web.util.Modules;
import org.lightcouch.CouchDbClient;
import org.lightcouch.NoDocumentException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by Sebastian Lauber on 11.03.2015.
 */
public class UserServlet extends HttpServlet {
    private static final String USER_PREFIX = "de.crawcial.user:";

    public static boolean isAdminParty(ServletContext sc) {
        try {
            CouchDbClient dbClient = new CouchDbClient(Modules.getCouchDbProperties(sc, Modules.CONFIGDB));
            return dbClient.view("crawcial/allUsers").query(JsonObject.class).size() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    protected static boolean verifySession(ServletContext sc, String s) throws IOException {
        if (s == null || !s.contains("|")) {
            return false;
        }
        String user = s.substring(0, s.indexOf("|"));
        CouchDbClient dbClient = new CouchDbClient(Modules.getCouchDbProperties(sc, Modules.CONFIGDB));
        try {
            JsonObject o = dbClient.find(JsonObject.class, USER_PREFIX + user);
            dbClient.shutdown();
            return (o.has(AuthServlet.COOKIE_NAME) && o.get(AuthServlet.COOKIE_NAME).getAsString().equals(s));
        } catch (Exception e) {
            dbClient.shutdown();
            return false;
        }
    }

    protected static Cookie verifyCredentials(ServletContext sc, String username, String password) throws IOException {
        CouchDbClient dbClient = new CouchDbClient(Modules.getCouchDbProperties(sc, Modules.CONFIGDB));
        try {
            JsonObject o = dbClient.find(JsonObject.class, USER_PREFIX + username);
            if (validatePassword(password, o.get("passhash").getAsString())) {
                dbClient.shutdown();
                return setSessionCookie(sc, username);
            }
        } catch (Exception e) {
            dbClient.shutdown();
            return null;
        }
        return null;
    }

    protected static Cookie setSessionCookie(ServletContext sc, String username) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        CouchDbClient dbClient = new CouchDbClient(Modules.getCouchDbProperties(sc, Modules.CONFIGDB));
        try {
            JsonObject o = dbClient.find(JsonObject.class, USER_PREFIX + username);
            Cookie sessionCookie = new Cookie(AuthServlet.COOKIE_NAME, username + "|" +
                    generateStrongPasswordHash(String.valueOf(Math.random())));
            o.addProperty(AuthServlet.COOKIE_NAME, sessionCookie.getValue());
            dbClient.update(o);
            dbClient.shutdown();
            return sessionCookie;
        } catch (Exception e) {
            return null;
        }
    }

    private static String generateStrongPasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = getSalt().getBytes();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }

    /*
    Source: http://howtodoinjava.com/2013/07/22/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
     */

    private static String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return new String(salt);
    }

    private static String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    private static boolean validatePassword(String originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = storedPassword.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        int diff = hash.length ^ testHash.length;
        for (int i = 0; i < hash.length && i < testHash.length; i++) {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (AuthHelper.isAuthenticated(req) && action != null) {
            CouchDbClient dbClient = new CouchDbClient(Modules.getCouchDbProperties(req.getServletContext(), Modules.CONFIGDB));
            JsonObject userJson;

            if (verifySession(req.getServletContext(), AuthHelper.getSessionCookie(req)) && action.equals("deluser")) {
                String cookie = AuthHelper.getSessionCookie(req);
                String cookieUsername = cookie.substring(0, cookie.indexOf("|"));
                userJson = dbClient.find(JsonObject.class, USER_PREFIX + cookieUsername);
                dbClient.remove(userJson);
                dbClient.shutdown();
                Cookie c = new Cookie(AuthServlet.COOKIE_NAME, "");
                c.setMaxAge(0);
                resp.addCookie(c);
                resp.sendRedirect(Modules.HOME);
            }
            if (req.getParameter("user") != null) {

                String username = req.getParameter("user").replaceAll("[^a-zA-Z0-9]+", "");
                try {
                    userJson = dbClient.find(JsonObject.class, USER_PREFIX + username);
                } catch (NoDocumentException e) {
                    userJson = null;
                }

                if (userJson == null && action.equals("adduser") && req.getParameter("password") != null) {
                    userJson = new JsonObject();
                    userJson.addProperty("_id", USER_PREFIX + username);
                    userJson.addProperty("name", username);
                    try {
                        userJson.addProperty("passhash", generateStrongPasswordHash(req.getParameter("password")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {

                        // Persist and retrieve passhash for verification
                        String returnedPassword = dbClient.find(JsonObject.class,
                                dbClient.save(userJson).getId()).get("passhash").getAsString();

                        if (validatePassword(req.getParameter("password"), returnedPassword)) {
                            resp.addCookie(verifyCredentials(getServletContext(), username, req.getParameter("password")));
                            resp.sendRedirect(Modules.HOME);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                dbClient.shutdown();
            }
        }
    }


}
