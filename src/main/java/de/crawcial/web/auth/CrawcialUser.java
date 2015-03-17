package de.crawcial.web.auth;

import de.crawcial.Constants;

/**
 * Created by Sebastian Lauber on 17.03.2015.
 */
public class CrawcialUser {
    private String _id;
    private String _rev;
    private String crawcialsession;
    private String name;
    private String passhash;

    public CrawcialUser(String name, String passhash) {
        _id = Constants.USER_PREFIX + name;
        this.name = name;
        this.passhash = passhash;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_rev() {
        return _rev;
    }

    public void set_rev(String _rev) {
        this._rev = _rev;
    }

    public String getCrawcialsession() {
        return crawcialsession;
    }

    public void setCrawcialsession(String crawcialsession) {
        this.crawcialsession = crawcialsession;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasshash() {
        return passhash;
    }

    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }
}
