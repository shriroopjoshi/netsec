package messages;

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
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("ERROR calculaing HASH");
        }
    }
    
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    
    public boolean differentTimeStamp(long currentTimeMillis) {
        return Math.abs(currentTimeMillis - Long.parseLong(this.timestamp)) < 60000;
    }
}
