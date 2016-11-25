package messages.otwayrees;

import com.google.gson.GsonBuilder;

/**
 *
 * @author shriroop
 */
public class FirstMessagePayload {

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
        return new GsonBuilder().create().fromJson(object, FirstMessagePayload.class);
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
