package messages;

import com.google.gson.GsonBuilder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shriroop
 */
public class AuthenticationMessage {

    private String username;
    private String password;
    private String timestamp;

    public AuthenticationMessage(String username, String password) {
        this.username = username;
        this.password = AuthenticationMessage.getPasswordHash(password);
        this.timestamp = System.currentTimeMillis() + "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static AuthenticationMessage getObjectFromString(String object) {
        return new GsonBuilder().create().fromJson(object, AuthenticationMessage.class);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }

    public static String getPasswordHash(String password) {
        String pass = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bs = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bs.length; i++) {
                sb.append(Integer.toHexString(0xFF & bs[i]));
            }
            pass = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AuthenticationMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pass;
    }

    public boolean differentTimeStamp(long currentTimeMillis) {
        return Math.abs(currentTimeMillis - Long.parseLong(this.timestamp)) < 60000;
    }

}
