package messages;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;

/**
 *
 * @author shriroop
 */
public class BuddylistMessage extends Message {

    ArrayList<String> buddies;

    public BuddylistMessage(ArrayList<String> buddies) {
        this.buddies = buddies;
    }

    public ArrayList<String> getBuddies() {
        return buddies;
    }
    
    public static BuddylistMessage getObjectFromString(String message) {
        return new GsonBuilder().create().fromJson(message, BuddylistMessage.class);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
