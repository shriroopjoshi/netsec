package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shriroop
 */
public class MessagingClient extends Thread{

    private final ServerSocket serverSocket;
    private final PublicKey serversPublicKey;
    private final PrivateKey privateKey;

    public MessagingClient(PublicKey serversPublicKey, PrivateKey privateKey, int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serversPublicKey = serversPublicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                MessagingClientForm form = new MessagingClientForm(socket);
            } catch (IOException ex) {
                Logger.getLogger(MessagingClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
