package server;

import exceptions.TamperedMessageException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import messages.AuthenticationMessage;
import messages.BuddylistMessage;
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
                
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException ex) {
                Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private String authenticateClient(Socket socket) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, TamperedMessageException {
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
                System.out.println(am.getUsername() + " logged in");
                // Success - Code 0
                onlineUsers.put(am.getUsername(), Boolean.TRUE);
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
        try {
            this.send(socket, message.toString(), publicKey);
        } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private int send(Socket socket, String message, PublicKey publicKey) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        DataOutputStream out;
        out = new DataOutputStream(socket.getOutputStream());
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int blockSize = 214;
        byte[] bytes = message.getBytes();
        int sizeMultiplier = bytes.length / blockSize;
        if (bytes.length % blockSize != 0) {
            sizeMultiplier += 1;
        }
        byte[] finalMessage = new byte[sizeMultiplier * 256];
        int counter = 0;
        int iteration = 1;
        for (int i = 0; i < bytes.length; i += blockSize) {
            int min = Math.min(bytes.length, blockSize * iteration);
            byte[] block = Arrays.copyOfRange(bytes, i, min);
            byte[] enc = cipher.doFinal(block);
            for (byte b : enc) {
                finalMessage[counter] = b;
                counter += 1;
            }
            iteration += 1;
        }
        out.write(finalMessage);
        return 0;
    }

    private String receive(Socket socket) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException {
        String finalMessage = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        // This is a crime. I've hard coded the volume of buffer
        byte[] message = new byte[2048];
        int size = in.read(message);
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        finalMessage = "";
        int blockSize = 256;
        int iteration = 1;
        for (int i = 0; i < size; i += blockSize) {
            int min = Math.min(message.length, blockSize * iteration);
            finalMessage += new String(cipher.doFinal(Arrays.copyOfRange(message, i, min)));
            iteration += 1;
        }
        return finalMessage;
    }

    private ArrayList<String> getBuddyList(String username) {
        return buddies.get(username);
    }
}
