package de.crawcial.web.auth;

import de.crawcial.Constants;

/**
 * Objects of this class represent a user and are used as POJOs.
 *
 * @author Sebastian Lauber
 */
public class CrawcialUser {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String _id;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String _rev;
    private String crawcialsession;
    private String name;
    private String passhash;

    /**
     * This constructor generates a new user object to be saved to Crawcial's CouchDB.
     *
     * @param name     the user name
     * @param passhash the hashed password
     */
    public CrawcialUser(String name, String passhash) {
        _id = Constants.USER_PREFIX + name;
        this.name = name;
        this.passhash = passhash;
    }

    /**
     * Returns the current session token.
     *
     * @return session token as string
     */
    public String getCrawcialsession() {
        return crawcialsession;
    }

    /**
     * Sets the current session token.
     *
     * @param crawcialsession session token as string
     */
    public void setCrawcialsession(String crawcialsession) {
        this.crawcialsession = crawcialsession;
    }

    /**
     * Returns the username.
     *
     * @return username
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the username.
     *
     * @param name username
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the password hash.
     *
     * @return password hash
     */
    public String getPasshash() {
        return passhash;
    }

    /**
     * Sets the password hash.
     *
     * @param passhash password hash
     */
    @SuppressWarnings("SameParameterValue")
    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }
}
