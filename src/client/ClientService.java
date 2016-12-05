package client;

import exceptions.TamperedMessageException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.swing.SwingUtilities;
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
    private String buddy;

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
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new MessagingClientForm(socket, key, false, username, buddy).setVisible(true);
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(AuthenticationClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TamperedMessageException ex) {
                Logger.getLogger(ClientService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private SecretKey authenticate(Socket socket) throws IOException, TamperedMessageException {
        SecretKey key = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        // Another crime!
        byte[] message = new byte[2048];
        int size = in.read(message);
        CommonUtility.verbose(Arrays.copyOfRange(message, 0, size), true);
        String req = new String(message);
        FirstMessage fm = FirstMessage.getObjectFromString(req);
        fm.verifyMessageHash();
        CommonUtility.verbose(fm.toString(), true);
        if (!fm.getReceiver().equalsIgnoreCase(this.username)) {
            return null;
        }
        int Nb = (int) Math.floor(Math.random() * 100);
        byte[] payload = fm.getPayload();
        FirstMessagePayload fmp = new FirstMessagePayload(Nb, fm.getNc(), fm.getSender(), fm.getReceiver());
        fmp.insertMessageHash();
        CommonUtility.verbose(fmp.toString(), true);
        byte[] encrypt = CommonUtility.encrypt(this.serversPublicKey, fmp.toString());
        CommonUtility.verbose(encrypt, true);
        DataOutputStream sout = new DataOutputStream(this.server.getOutputStream());
        sout.write(payload);
        sout.write(encrypt);
        // Server generated keys, and sends it back
        DataInputStream sin = new DataInputStream(this.server.getInputStream());
        // An offence again!
        byte[] data = new byte[65536];
        int sz = sin.read(data);
        byte[] copyOfRange = Arrays.copyOfRange(data, 0, sz);
        CommonUtility.verbose(copyOfRange, true);
        SecondMessage sm = SecondMessage.getObjectFromString(new String(copyOfRange));
        sm.verifyMessageHash();
        CommonUtility.verbose(sm.toString(), true);
        byte[] payloadTwo = sm.getPayloadTwo();
        CommonUtility.verbose(payloadTwo, true);
        String payloadTwoString = CommonUtility.decrypt(this.privateKey, payloadTwo, payloadTwo.length);
        SecondMessagePayload payloadT = SecondMessagePayload.getObjectFromString(payloadTwoString);
        payloadT.verifyMessageHash();
        CommonUtility.verbose(payloadT.toString(), true);
        if (payloadT.getN() == Nb) {
            key = payloadT.getSecretKey();
        }
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write(sm.getPayloadOne());
        this.buddy = fm.getSender();
        return key;
    }
}
