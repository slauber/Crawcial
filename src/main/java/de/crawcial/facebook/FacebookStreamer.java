package de.crawcial.facebook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.crawcial.facebook.gson.Change;
import de.crawcial.facebook.gson.Entry;
import de.crawcial.facebook.gson.FbChange;
import de.crawcial.facebook.gson.Value;
import facebook4j.Facebook;
import facebook4j.auth.AccessToken;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

/**
 * This singleton class handles the management of the Facebook Streamer.
 *
 * @author Sebastian Lauber
 */
public class FacebookStreamer {
    private static Facebook fb;
    private static AccessToken token;

    /**
     * This method sets the facebook4j client and the user access token.
     *
     * @param fb    preconfigured facebook4j client
     * @param token Facebook user access token
     */
    public static void setFbVars(Facebook fb, AccessToken token) {
        FacebookStreamer.fb = fb;
        FacebookStreamer.token = token;
    }

    /**
     * This method parses the changes received by the FbServlet. This is todo
     *
     * @param jsonString   verified incoming message from Facebook
     * @param dbProperties CouchDbProperties pointing to the Crawcial Facebook database
     */
    public static void parseChange(String jsonString, CouchDbProperties dbProperties) {
        Gson gson = new Gson();
        FbChange change = gson.fromJson(jsonString, FbChange.class);
        //SimpleDateFormat sdf = new SimpleDateFormat();
        for (Entry e : change.getEntry()) {
            CouchDbClient dbClient = new CouchDbClient(dbProperties);
            JsonObject pageJson = null;
            try {
                pageJson = dbClient.find(JsonObject.class, e.getId());
                pageJson.addProperty("lastupdate", e.getTime());
            } catch (Exception ex) {
                // Don't care
            }
            dbClient.shutdown();
            for (Change c : e.getChanges()) {
                Value v = c.getValue();
                switch (v.getVerb()) {
                    case "add":
                        switch (v.getItem()) {
                            case "comment":
                                addComment(pageJson, v.getCommentId(), v.getParentId());
                                break;
                            case "like":
                                addLike(pageJson, v.getParentId(), String.valueOf(v.getSenderId()));
                                break;
                            default:
                                addStatus(pageJson, v.getPostId());
                                break;
                        }
                        break;
                    case "edited": {
                        switch (v.getItem()) {
                            case "comment":
                                editComment(pageJson, v.getCommentId(), v.getParentId());
                                break;
                            default:
                                editStatus(pageJson, v.getPostId());
                                break;
                        }
                    }
                    break;
                    case "remove":
                        switch (v.getItem()) {
                            case "comment":
                                removeComment(pageJson, v.getCommentId(), v.getParentId());
                                break;
                            case "like":
                                removeLike(pageJson, v.getParentId(), String.valueOf(v.getSenderId()));
                                break;
                            default:
                                removeStatus(pageJson, v.getPostId());
                                break;
                        }
                        break;
                    case "hide":
                        switch (v.getItem()) {
                            case "comment":
                                hideComment(pageJson, v.getCommentId(), v.getParentId());
                                break;
                            default:
                                hideStatus(pageJson, v.getPostId());
                                break;
                        }
                        break;
                }
                /*
                outString.append("\t\tField: ");
                outString.append(c.getField());
                if (v.getCreatedTime() != null) {
                    outString.append("\tCreated time: ");
                    outString.append(sdf.format(new Date(v.getCreatedTime() * 1000)));
                }
                outString.append("\tItem: ");
                outString.append(v.getItem());
                outString.append("\tPost: ");
                outString.append(v.getPostId());
                outString.append("\tParent: ");
                outString.append(v.getParentId());
                outString.append("\tSender: ");
                outString.append(v.getSenderId());
                outString.append("\tComment: ");
                outString.append(v.getCommentId());
                outString.append("\tVerb: ");
                outString.append(v.getVerb());
                */
            }

        }
    }

    /**
     * Handles a status addition.
     *
     * @param pageJson Facebook page JSON from database
     * @param statusId added status ID
     * @return updated Facebook page JSON
     */
    private static JsonObject addStatus(JsonObject pageJson, String statusId) {
        System.out.println("Add statusId: " + statusId);
        return pageJson;
    }

    /**
     * Handles a like addition.
     *
     * @param pageJson Facebook page JSON from database
     * @param parentId the change parent ID
     * @param senderId the change sender ID
     * @return updated Facebook page JSON
     */
    private static JsonObject addLike(JsonObject pageJson, String parentId, String senderId) {
        System.out.println("Add like parentId: " + parentId + " senderId: " + senderId);
        return pageJson;
    }

    /**
     * Handles a comment addition.
     *
     * @param pageJson  Facebook page JSON from database
     * @param commentId the added comment ID
     * @param parentId  the change parent ID
     * @return updated Facebook page JSON
     */
    private static JsonObject addComment(JsonObject pageJson, String commentId, String parentId) {
        System.out.println("Add comment parentId: " + parentId + " commentId: " + commentId);
        return pageJson;
    }

    /**
     * Handles a status edit.
     *
     * @param pageJson Facebook page JSON from database
     * @param statusId the changed status
     * @return updated Facebook page JSON
     */
    private static JsonObject editStatus(JsonObject pageJson, String statusId) {
        System.out.println("Edit statusId: " + statusId);
        return pageJson;
    }

    /**
     * Handles a comment edit.
     *
     * @param pageJson  Facebook page JSON from database
     * @param commentId the changed comment
     * @param parentId  the change parent ID
     * @return updated Facebook page JSON
     */
    private static JsonObject editComment(JsonObject pageJson, String commentId, String parentId) {
        System.out.println("Edit comment parentId: " + parentId + " commentId: " + commentId + " parentId: " + parentId);
        return pageJson;
    }

    /**
     * Handles a status removal.
     *
     * @param pageJson Facebook page JSON from database
     * @param statusId the removed status ID
     * @return updated Facebook page JSON
     */
    private static JsonObject removeStatus(JsonObject pageJson, String statusId) {
        System.out.println("Remove statusId: " + statusId);
        return pageJson;
    }

    /**
     * Handles a like removal.
     *
     * @param pageJson Facebook page JSON from database
     * @param parentId the change parent ID
     * @param senderId the change sender ID
     * @return updated Facebook page JSON
     */
    private static JsonObject removeLike(JsonObject pageJson, String parentId, String senderId) {
        System.out.println("Remove like parentId: " + parentId + " senderId: " + senderId);
        return pageJson;
    }

    /**
     * Handles a comment removal.
     *
     * @param pageJson  Facebook page JSON from database
     * @param commentId the removed comment ID
     * @param parentId  the change parent ID
     * @return updated Facebook page JSON
     */
    private static JsonObject removeComment(JsonObject pageJson, String commentId, String parentId) {
        System.out.println("Remove comment parentId: " + parentId + " commentId: " + commentId + " parentId: " + parentId);
        return pageJson;
    }

    /**
     * Handles a comment hiding.
     *
     * @param pageJson  Facebook page JSON from database
     * @param commentId the hidden comment ID
     * @param parentId  the change parent ID
     * @return updated Facebook page JSON
     */
    private static JsonObject hideComment(JsonObject pageJson, String commentId, String parentId) {
        System.out.println("Hide comment parentId: " + parentId + " commentId: " + commentId + " parentId: " + parentId);
        return pageJson;
    }

    /**
     * Handles a status hiding.
     *
     * @param pageJson Facebook page JSON from database
     * @param statusId the hidden status ID
     * @return updated Facebook page JSON
     */
    private static JsonObject hideStatus(JsonObject pageJson, String statusId) {
        System.out.println("Hide statusId: " + statusId);
        return pageJson;
    }

}
