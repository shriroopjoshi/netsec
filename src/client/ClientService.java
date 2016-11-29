package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import messages.otwayrees.FirstMessage;
import messages.otwayrees.FirstMessagePayload;
import messages.otwayrees.SecondMessage;
import messages.otwayrees.SecondMessagePayload;
import utility.CommonUtility;

/**
 *
 * @author shriroop
 */
public class ClientService extends Thread {

    private final ServerSocket serverSocket;
    private final String username;
    private final PublicKey serversPublicKey;
    private final PrivateKey privateKey;
    private final Socket server;

    public ClientService(ServerSocket serverSocket, String username,
            PublicKey serversPublicKey, PrivateKey privateKey, Socket server) {
        this.serverSocket = serverSocket;
        this.username = username;
        this.privateKey = privateKey;
        this.serversPublicKey = serversPublicKey;
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                // Do the authentication part - accepting messages
                SecretKey key = this.authenticate(socket);
                MessagingClientForm form = new MessagingClientForm(socket, key);
            } catch (IOException ex) {
                Logger.getLogger(AuthenticationClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private SecretKey authenticate(Socket socket) throws IOException {
        SecretKey key = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        // Another crime!
        byte[] message = new byte[2048];
        int size = in.read(message);
        String req = new String(message);
        System.out.println("Request: " + req);
        FirstMessage fm = FirstMessage.getObjectFromString(req);
        if (!fm.getReceiver().equalsIgnoreCase(this.username)) {
            return null;
        }
        int Nb = (int) Math.floor(Math.random() * 100);
        byte[] payload = fm.getPayload();
        FirstMessagePayload fmp = new FirstMessagePayload(Nb, fm.getNc(), fm.getSender(), fm.getReceiver());
        byte[] encrypt = CommonUtility.encrypt(this.serversPublicKey, fmp.toString());
        DataOutputStream sout = new DataOutputStream(this.server.getOutputStream());
        sout.write(payload);
        sout.write(encrypt);
        /**
         * No programming done on server to read these messages and generate Secret key
         */
        DataInputStream sin = new DataInputStream(this.server.getInputStream());
        // An offence again!
        byte[] data = new byte[2048];
        int sz = sin.read(data);
        String decrypt = CommonUtility.decrypt(privateKey, message, sz);
        SecondMessage sm = SecondMessage.getObjectFromString(decrypt);
        byte[] payloadTwo = sm.getPayloadTwo();
        String payloadTwoString = CommonUtility.decrypt(this.privateKey, payloadTwo, payloadTwo.length);
        SecondMessagePayload payloadT = SecondMessagePayload.getObjectFromString(payloadTwoString);
        if(payloadT.getN() == Nb) {
            key = payloadT.getSecretKey();
        }
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write(sm.getPayloadOne());
        return key;
    }

}
