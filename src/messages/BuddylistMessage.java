package messages;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import utility.Pair;

/**
 *
 * @author shriroop
 */
public class BuddylistMessage extends Message {

    private final ArrayList<Pair<String, String>> buddyList;

    public BuddylistMessage(ArrayList<Pair<String, String>> buddyList) {
        super();
        this.buddyList = buddyList;
    }

    public static BuddylistMessage getObjectFromString(String object) {
        return new GsonBuilder().create().fromJson(object, BuddylistMessage.class);
    }

    public ArrayList<Pair<String, String>> getBuddyList() {
        return buddyList;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
