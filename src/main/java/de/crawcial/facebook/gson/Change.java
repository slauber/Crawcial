package de.crawcial.facebook.gson;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Change {

    @Expose
    private String field;
    @Expose
    private Value value;

    /**
     * @return The field
     */
    public String getField() {
        return field;
    }

    /**
     * @param field The field
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * @return The value
     */
    public Value getValue() {
        return value;
    }

    /**
     * @param value The value
     */
    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(field).append(value).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Change) == false) {
            return false;
        }
        Change rhs = ((Change) other);
        return new EqualsBuilder().append(field, rhs.field).append(value, rhs.value).isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
