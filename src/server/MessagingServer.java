package server;

import com.google.gson.GsonBuilder;
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
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import messages.AuthenticationMessage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
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
    private HashMap<String, ArrayList<String>> buddyList;

    public MessagingServer() {
        users = new HashMap<>();
        onlineUsers = new HashMap<>();
        String configFile = "resources/config.properties";
        Properties configs = Initialize.loadProperties(configFile);
        Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port"));
        Constants.CIPHER_TYPE = configs.getProperty("app.cipher.type");
        Constants.BUDDY_LIST = configs.getProperty("server.buddylist");
        Constants.CLIENT_KEY_PREFIX = configs.getProperty("client.key.path");
        try {
            users = Initialize.getUsers(configs.getProperty("app.users.file"));
            users.keySet().stream().forEach((user) -> {
                onlineUsers.put(user, Boolean.FALSE);
            });
            privateKey = Initialize.getPrivateKey(configs.getProperty("server.privatekey"),
                    configs.getProperty("app.algorithm"));
            this.buddyList = Initialize.readBuddyList(Constants.BUDDY_LIST);
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
                String username = this.authenticateClient(socket);
                this.sendBuddyList(socket, username);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private String authenticateClient(Socket socket) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        String username = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        ArrayList<Byte> mess = new ArrayList<>();
        int blockSize = 256;
        byte b = 0;
        b = (byte) in.read();
        while(b >= 0) {
            mess.add(b);
            b = (byte) in.read();
        }
        byte[] message = new byte[mess.size()];
        int i = 0;
        for (Byte by : mess) {
            message[i++] = by;
        }
        System.out.println("mess");
        //in.readFully(message);
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        String finalMessage = new String(cipher.doFinal(message));
        AuthenticationMessage am = AuthenticationMessage.getObjectFromString(finalMessage);
        if (!am.correctHash(am.digest)) {
            // Message tampered. Relay to client
            throw new NotImplementedException();
        }
        System.out.println("REQUEST:\n" + am);
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
                username = am.getUsername();
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
        return username;
    }

    private void sendBuddyList(Socket socket, String username) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        ArrayList<String> list = buddyList.get(username);
        String message = new GsonBuilder().create().toJson(list);
        System.out.println("buddy list: " + message);
        byte[] finalMessage = null;
        try {
            finalMessage = this.getEncryptedMessage(message, username);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeySpecException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        out.write(finalMessage);
    }

    private byte[] getEncryptedMessage(String message, String username) throws NoSuchAlgorithmException,
            NoSuchPaddingException, IOException, InvalidKeySpecException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
        PublicKey publicKey = Initialize.getPublicKey(
                Constants.CLIENT_KEY_PREFIX + username + Constants.CLIENT_PUBLIC_KEY_SUFFIX,
                Constants.PUBLIC_KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] finalMessage = cipher.doFinal(message.getBytes());
        return finalMessage;
    }
}
