package client;

import com.sun.org.apache.bcel.internal.util.ByteSequence;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import messages.otwayrees.FirstMessage;
import utility.Constants;
import utility.Pair;

/**
 *
 * @author shriroop
 */
public class AuthenticationClient {

    private final PublicKey serversPublicKey;
    private final PrivateKey privateKey;
    private final Pair<String, SocketAddress> buddy;
    private final String username;

    public AuthenticationClient(PublicKey serversPublicKey, PrivateKey privateKey,
            Pair<String, SocketAddress> buddy, String username) throws IOException {
        this.serversPublicKey = serversPublicKey;
        this.privateKey = privateKey;
        this.buddy = buddy;
        this.username = username;
    }

    public void authenticate() throws IOException {
        Socket socket = new Socket(InetAddress.getByName(buddy.getSecond().toString()), Constants.CLIENT_PORT);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        FirstMessage fm = new FirstMessage(this.username, buddy.getFirst(), serversPublicKey);
    }
}
