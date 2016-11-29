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
public class SecondMessage extends Message {

    private final int Nc;
    private final byte[] payloadOne;
    private final byte[] payloadTwo;

    public SecondMessage(int Nc, byte[] payloadOne, byte[] payloadTwo) {
        this.Nc = Nc;
        this.payloadOne = payloadOne;
        this.payloadTwo = payloadTwo;
    }

    public int getNc() {
        return Nc;
    }

    public byte[] getPayloadOne() {
        return payloadOne;
    }

    public byte[] getPayloadTwo() {
        return payloadTwo;
    }

    public static SecondMessage getObjectFromString(String object) {
        Gson create = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new StringReader(object));
        reader.setLenient(true);
        return create.fromJson(reader, SecondMessage.class);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }

}
