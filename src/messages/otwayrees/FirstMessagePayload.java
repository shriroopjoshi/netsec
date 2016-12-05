package messages.otwayrees;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import messages.Message;

/**
 *
 * @author shriroop
 */
public class FirstMessagePayload extends Message {

    private final int Na;
    private final int Nc;
    private final String sender;
    private final String receiver;

    public FirstMessagePayload(int Na, int Nc, String sender, String receiver) {
        this.Na = Na;
        this.Nc = Nc;
        this.sender = sender;
        this.receiver = receiver;
    }

    public static FirstMessagePayload getObjectFromString(String object) {
        Gson create = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new StringReader(object));
        reader.setLenient(true);
        return create.fromJson(reader, FirstMessagePayload.class);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }

    public int getNa() {
        return Na;
    }

    public int getNc() {
        return Nc;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }
}
