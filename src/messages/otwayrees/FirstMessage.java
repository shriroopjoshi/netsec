package messages.otwayrees;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.security.PublicKey;
import messages.Message;
import utility.CommonUtility;

/**
 *
 * @author shriroop
 */
public class FirstMessage extends Message {

    private final int Na;
    private final int Nc;
    private final String sender;
    private final String receiver;
    private final byte[] payload;

    public FirstMessage(String sender, String receiver, PublicKey serversPublicKey) {
        Na = (int) Math.floor(Math.random() * 100);
        Nc = (int) Math.floor(Math.random() * 100);
        this.sender = sender;
        this.receiver = receiver;
        FirstMessagePayload fmp = new FirstMessagePayload(Na, Nc, this.sender, this.receiver);
        fmp.insertMessageHash();
        this.payload = CommonUtility.encrypt(serversPublicKey, fmp.toString());
    }
    
    public int getNa() {
        return this.Na;
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
        Gson create = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new StringReader(object));
        reader.setLenient(true);
        return create.fromJson(reader, FirstMessage.class);        
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
