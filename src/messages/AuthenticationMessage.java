package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shriroop
 */
public class AuthenticationMessage extends Message {

    private String username;
    private String password;  

    public AuthenticationMessage(String username, String password) {
        super();
        this.username = username;
        this.password = AuthenticationMessage.getPasswordHash(password);
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

    public static AuthenticationMessage getObjectFromString(String object) {
        Gson create = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new StringReader(object));
        reader.setLenient(true);
        return create.fromJson(reader, AuthenticationMessage.class);
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
}
