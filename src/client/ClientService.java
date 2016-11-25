package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shriroop
 */
public class ClientService extends Thread {
    private final ServerSocket serverSocket;

    public ClientService(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                // Do the authentication part - accepting messages
                MessagingClientForm form = new MessagingClientForm(socket);
            } catch (IOException ex) {
                Logger.getLogger(AuthenticationClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
}
