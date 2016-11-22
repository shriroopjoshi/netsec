package messages;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shriroop
 */
public abstract class Message {

    public String digest;
    public String timestamp;

    public String calculateHash() {
        digest = null;
        try {
            JsonObject json = new GsonBuilder().create().toJsonTree(this).getAsJsonObject();//.remove("digest").getAsJsonObject().remove("timestamp");
            if(json.has("digest")) {
                json.remove("digest");
            }
            if(json.has("timestamp")) {
                json.remove("timestamp");
            }
            String message = new GsonBuilder().create().toJson(json);
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(message.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte i : bytes) {
                sb.append(Integer.toHexString(0xFF & i));
            }
            this.digest = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
        }
        return digest;
    }

    public boolean correctHash(String hash) {
        try {
            JsonElement json = new GsonBuilder().create().toJsonTree(this).getAsJsonObject().remove("digest").getAsJsonObject().remove("timestamp");
            String message = new GsonBuilder().create().toJson(json);
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(message.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte i : bytes) {
                sb.append(Integer.toHexString(0xFF & i));
            }
            return sb.toString().equals(hash);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void insertTimestamp() {
        this.timestamp = System.currentTimeMillis() + "";
    }

    public boolean differentTimeStamp(long currentTimeMillis) {
        return Math.abs(currentTimeMillis - Long.parseLong(this.timestamp)) < 60000;
    }
}
