package messages.otwayrees;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import utility.Constants;

/**
 *
 * @author shriroop
 */
public class SecondMessagePayload {

    private final int N;
    private final String secretKey;

    public SecondMessagePayload(int N, SecretKey secretKey) {
        this.N = N;
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public int getN() {
        return N;
    }

    public SecretKey getSecretKey() {
        byte[] decode = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(decode, 0, decode.length, Constants.SECRET_KEY_ALGO);
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
