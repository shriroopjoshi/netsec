package messages.otwayrees;

import com.google.gson.GsonBuilder;
import java.security.PublicKey;
import messages.Message;
import utility.CommonUtility;

/**
 *
 * @author shriroop
 */
public class FirstMessage extends Message {

    private final int Nc;
    private final String sender;
    private final String receiver;
    private final byte[] payload;

    public FirstMessage(String sender, String receiver, PublicKey serversPublicKey) {
        int Na = (int) Math.floor(Math.random() * 100);
        Nc = (int) Math.floor(Math.random() * 100);
        this.sender = sender;
        this.receiver = receiver;
        FirstMessagePayload fmp = new FirstMessagePayload(Na, Nc, this.sender, this.receiver);
        this.payload = CommonUtility.encrypt(serversPublicKey, fmp.toString());
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

    public byte[] getPayload() {
        return payload;
    }

    public static FirstMessage getObjectFromString(String object) {
        return new GsonBuilder().create().fromJson(object, FirstMessage.class);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
