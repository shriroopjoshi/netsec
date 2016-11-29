package messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
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
        Gson create = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new StringReader(object));
        reader.setLenient(true);
        return create.fromJson(reader, BuddylistMessage.class);
    }

    public ArrayList<Pair<String, String>> getBuddyList() {
        return buddyList;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
