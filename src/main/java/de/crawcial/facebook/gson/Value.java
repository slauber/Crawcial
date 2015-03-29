package de.crawcial.facebook.gson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Value {

    @Expose
    private String item;
    @Expose
    private String verb;
    @SerializedName("parent_id")
    @Expose
    private String parentId;
    @SerializedName("sender_id")
    @Expose
    private Long senderId;
    @SerializedName("comment_id")
    @Expose
    private String commentId;
    @SerializedName("post_id")
    @Expose
    private String postId;
    @SerializedName("created_time")
    @Expose
    private Long createdTime;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    /**
     * @return The item
     */
    public String getItem() {
        return item;
    }

    /**
     * @param item The item
     */
    public void setItem(String item) {
        this.item = item;
    }

    /**
     * @return The verb
     */
    public String getVerb() {
        return verb;
    }

    /**
     * @param verb The verb
     */
    public void setVerb(String verb) {
        this.verb = verb;
    }

    /**
     * @return The parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @param parentId The parent_id
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * @return The senderId
     */
    public Long getSenderId() {
        return senderId;
    }

    /**
     * @param senderId The sender_id
     */
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    /**
     * @return The createdTime
     */
    public Long getCreatedTime() {
        return createdTime;
    }

    /**
     * @param createdTime The created_time
     */
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(item).append(verb).append(parentId).append(senderId).append(createdTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Value) == false) {
            return false;
        }
        Value rhs = ((Value) other);
        return new EqualsBuilder().append(item, rhs.item).append(verb, rhs.verb).append(parentId, rhs.parentId).append(senderId, rhs.senderId).append(createdTime, rhs.createdTime).isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
