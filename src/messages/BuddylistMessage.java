package messages;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;

/**
 *
 * @author shriroop
 */
public class BuddylistMessage extends Message {
    private ArrayList<String> buddyList;

    public BuddylistMessage(ArrayList<String> buddyList) {
        super();
        this.buddyList = buddyList;
    }

    public static BuddylistMessage getObjectFromString(String object) {
        return new GsonBuilder().create().fromJson(object, BuddylistMessage.class);
    }

    public ArrayList<String> getBuddyList() {
        return buddyList;
    }
    
    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
