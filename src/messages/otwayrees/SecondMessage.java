package messages.otwayrees;

import com.google.gson.GsonBuilder;
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
        return new GsonBuilder().create().fromJson(object, SecondMessage.class);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }

}
