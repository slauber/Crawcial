package de.crawcial.facebook.gson;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class FbChange {

    @Expose
    private String object;
    @Expose
    private List<Entry> entry = new ArrayList<Entry>();

    /**
     * @return The object
     */
    public String getObject() {
        return object;
    }

    /**
     * @param object The object
     */
    public void setObject(String object) {
        this.object = object;
    }

    /**
     * @return The entry
     */
    public List<Entry> getEntry() {
        return entry;
    }

    /**
     * @param entry The entry
     */
    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(object).append(entry).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FbChange) == false) {
            return false;
        }
        FbChange rhs = ((FbChange) other);
        return new EqualsBuilder().append(object, rhs.object).append(entry, rhs.entry).isEquals();
    }

}
