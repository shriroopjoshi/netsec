package messages;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author shriroop
 */
public class Message {
    
    protected String timestamp;
    protected String messageHash;

    public Message() {
        this.timestamp = System.currentTimeMillis() + "";
    }
    
    public void insertMessageHash() {
        this.messageHash = this.getMessageHash();
    }
    
    private String getMessageHash() {
        String hash = null;
        JsonObject json = new GsonBuilder().create().toJsonTree(this).getAsJsonObject();
        if(json.has("timestamp"))
            json.remove("timestamp");
        if(json.has("messageHash"))
            json.remove("messageHash");
        String message = new GsonBuilder().create().toJson(json);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(message.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(Integer.toHexString(0xFF & b));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("ERROR calculaing HASH");
        }
        return hash;
    }
    
    public boolean verifyMessageHash() {
        String hash = this.getMessageHash();
        return hash.equalsIgnoreCase(messageHash);
    }
    
    public String getTimestamp() {
        return timestamp;
    }

    public void insertTimestamp() {
        this.timestamp = System.currentTimeMillis() + "";
    }
    
    
    public boolean differentTimeStamp(long currentTimeMillis) {
        return Math.abs(currentTimeMillis - Long.parseLong(this.timestamp)) < 60000;
    }
}
