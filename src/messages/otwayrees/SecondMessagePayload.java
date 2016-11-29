package messages.otwayrees;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
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
        Gson create = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new StringReader(object));
        reader.setLenient(true);
        return create.fromJson(reader, SecondMessagePayload.class);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
