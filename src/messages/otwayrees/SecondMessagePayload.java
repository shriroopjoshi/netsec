package messages.otwayrees;

import com.google.gson.GsonBuilder;
import javax.crypto.SecretKey;

/**
 *
 * @author shriroop
 */
public class SecondMessagePayload {

    private final int N;
    private final SecretKey secretKey;

    public SecondMessagePayload(int N, SecretKey secretKey) {
        this.N = N;
        this.secretKey = secretKey;
    }

    public int getN() {
        return N;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public static SecondMessagePayload getObjectFromString(String object) {
        return new GsonBuilder().create().fromJson(object, SecondMessagePayload.class);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
