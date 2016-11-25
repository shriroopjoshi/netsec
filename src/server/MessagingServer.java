package server;

import exceptions.TamperedMessageException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.AuthenticationMessage;
import messages.BuddylistMessage;
import utility.CommonUtility;
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
    private HashMap<String, SocketAddress> onlineUsers;
    private HashMap<String, ArrayList<String>> buddies;

    public MessagingServer() {
        users = new HashMap<>();
        onlineUsers = new HashMap<>();
        String configFile = "resources/config.properties";
        Properties configs = Initialize.loadProperties(configFile);
        Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port"));
        Constants.CIPHER_TYPE = configs.getProperty("app.cipher.type");
        Constants.CLIENT_KEYS_PATH = configs.getProperty("client.keys.path");
        Constants.PUBLIC_KEY_ALGO = configs.getProperty("app.algorithm");
        try {
            users = Initialize.getUsers(configs.getProperty("app.users.file"));
            buddies = Initialize.readBuddyList(configs.getProperty("app.buddy.list.file"));
            users.keySet().stream().forEach((user) -> {
                onlineUsers.put(user, null);
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

    public void start() throws IOException, TamperedMessageException {
        System.out.println("Server started");
        while (true) {
            Socket socket = serverSocket.accept();
            try {
                String username = this.authenticateClient(socket);
                this.sendBuddyList(socket, username);
                /**
                 * Space to implement KDC logic
                 */
            } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private String authenticateClient(Socket socket) throws IOException, TamperedMessageException {
        String username = null;
        String finalMessage = this.receive(socket);
        AuthenticationMessage am = AuthenticationMessage.getObjectFromString(finalMessage);
        if (!am.verifyMessageHash()) {
            throw new TamperedMessageException();
        }
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
                SocketAddress address = socket.getRemoteSocketAddress();
                System.out.println(am.getUsername() + " logged in from " + address);
                // Success - Code 0
                onlineUsers.put(am.getUsername(), address);
                username = am.getUsername();
                out.writeInt(0);
            }
        } else {
            // Denied
            // Incorrect password - Code 1
            out.writeInt(1);
        }
        //out.close();
        return username;
    }

    private void sendBuddyList(Socket socket, String username) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PublicKey publicKey = Initialize.getPublicKey(Constants.CLIENT_KEYS_PATH + username + Constants.CLIENT_PUBLIC_KEY_SUFFIX, Constants.PUBLIC_KEY_ALGO);
        BuddylistMessage message = new BuddylistMessage(this.getBuddyList(username));
        message.insertMessageHash();
        this.send(socket, message.toString(), publicKey);
    }

    private int send(Socket socket, String message, PublicKey publicKey) throws IOException {
        DataOutputStream out;
        out = new DataOutputStream(socket.getOutputStream());
        byte[] finalMessage = CommonUtility.encrypt(publicKey, message);
        out.write(finalMessage);
        return 0;
    }

    private String receive(Socket socket) throws IOException {
        String finalMessage = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        // This is a crime. I've hard coded the volume of buffer
        byte[] message = new byte[2048];
        int size = in.read(message);
        finalMessage = CommonUtility.decrypt(privateKey, message, size);
        return finalMessage;
    }

    private ArrayList<String> getBuddyList(String username) {
        return buddies.get(username);
    }
}
