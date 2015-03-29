package de.crawcial.facebook.gson;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Entry {

    @Expose
    private String id;
    @Expose
    private Long time;
    @Expose
    private List<Change> changes = new ArrayList<Change>();

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The time
     */
    public Long getTime() {
        return time;
    }

    /**
     * @param time The time
     */
    public void setTime(Long time) {
        this.time = time;
    }

    /**
     * @return The changes
     */
    public List<Change> getChanges() {
        return changes;
    }

    /**
     * @param changes The changes
     */
    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(time).append(changes).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Entry) == false) {
            return false;
        }
        Entry rhs = ((Entry) other);
        return new EqualsBuilder().append(id, rhs.id).append(time, rhs.time).append(changes, rhs.changes).isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
