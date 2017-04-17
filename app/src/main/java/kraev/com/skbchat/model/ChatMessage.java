package kraev.com.skbchat.model;

/**
 * Created by qbai on 13.04.2017.
 */

public class ChatMessage {

    private String text;
    private String name;
    private String photoUrl;
    private String avatarUrl;
    private String senderUid;

    public ChatMessage() {
    }

    public ChatMessage(String text, String name, String photoUrl, String avatarUrl, String uid) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.avatarUrl = avatarUrl;
        this.senderUid = uid;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
