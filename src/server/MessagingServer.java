package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import messages.AuthenticationMessage;
import utility.Constants;
import utility.Initialize;

/**
 *
 * @author shriroop
 */
public class MessagingServer {

    private ServerSocket serverSocket;
    private PrivateKey privateKey;
    private HashMap<String, String> users;
    private HashMap<String, Boolean> onlineUsers;

    public MessagingServer() {
        users = new HashMap<>();
        onlineUsers = new HashMap<>();
        String configFile = "resources/config.properties";
        Properties configs = Initialize.loadProperties(configFile);
        Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port"));
        Constants.CIPHER_TYPE = configs.getProperty("app.cipher.type");
        try {
            users = Initialize.getUsers(configs.getProperty("app.users.file"));
            users.keySet().stream().forEach((user) -> {
                onlineUsers.put(user, Boolean.FALSE);
            });
            privateKey = Initialize.getPrivateKey(configs.getProperty("server.privatekey"),
                    configs.getProperty("app.algorithm"));
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Unable to instatiate server");
            System.exit(1);
        }
    }

    public void start() throws IOException {
        System.out.println("Server started");
        while (true) {
            Socket socket = serverSocket.accept();
            try {
                this.authenticateClient(socket);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private void authenticateClient(Socket socket) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        byte[] message = new byte[256];
        in.readFully(message);
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        String finalMessage = new String(cipher.doFinal(message));
        AuthenticationMessage am = AuthenticationMessage.getObjectFromString(finalMessage);
        System.out.println("MESSAGE:\n" + am);
        String password = users.get(am.getUsername());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        if (password == null) {
            // Denied
            // No such user - Code 2
            out.writeInt(2);
        } else if (password.equals(am.getPassword())) {
            if (!am.differentTimeStamp(System.currentTimeMillis())) {
                // Denied
                // Replay attack - Code 3
                out.writeInt(3);
            } else {
                System.out.println(am.getUsername() + " logged in");
                // Success - Code 0
                out.writeInt(0);
                onlineUsers.put(am.getUsername(), Boolean.TRUE);
            }
        } else {
            // Denied
            // Incorrect password - Code 1
            out.writeInt(1);
        }
        out.close();
        in.close();
    }
}
