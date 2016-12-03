package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import javax.swing.SwingUtilities;
import messages.otwayrees.FirstMessage;
import messages.otwayrees.SecondMessagePayload;
import utility.CommonUtility;
import utility.Constants;
import utility.Pair;

/**
 *
 * @author shriroop
 */
public class AuthenticationClient {

    private final PublicKey serversPublicKey;
    private final PrivateKey privateKey;
    private final Pair<String, String> buddy;
    private final String username;

    public AuthenticationClient(PublicKey serversPublicKey, PrivateKey privateKey,
            Pair<String, String> buddy, String username) throws IOException {
        this.serversPublicKey = serversPublicKey;
        this.privateKey = privateKey;
        this.buddy = buddy;
        this.username = username;
    }

    public void authenticate() throws IOException {
        Socket socket = new Socket(InetAddress.getByName(buddy.getSecond()), Constants.CLIENT_PORT);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        FirstMessage fm = new FirstMessage(this.username, buddy.getFirst(), serversPublicKey);
        System.out.println(fm);
        out.write(fm.toString().getBytes());
        byte[] b = new byte[2048];
        int sz = in.read(b);
        String msg = CommonUtility.decrypt(privateKey, b, sz);
        SecondMessagePayload smp = SecondMessagePayload.getObjectFromString(msg);
        if(fm.getNa() == smp.getN()) {
            SecretKey secretKey = smp.getSecretKey();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new MessagingClientForm(socket, secretKey, true, username, buddy.getFirst()).setVisible(true);
                }
            });
        }
    }

}
