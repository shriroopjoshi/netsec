package server;

import exceptions.TamperedMessageException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import messages.AuthenticationMessage;
import messages.BuddylistMessage;
import messages.otwayrees.FirstMessagePayload;
import messages.otwayrees.SecondMessage;
import messages.otwayrees.SecondMessagePayload;
import utility.CommonUtility;
import utility.Constants;
import utility.Initialize;
import utility.Pair;

/**
 *
 * @author shriroop
 */
public class MessagingServer {

    private ServerSocket serverSocket;
    private PrivateKey privateKey;
    private HashMap<String, String> users;
    private HashMap<String, InetSocketAddress> onlineUsers;
    private HashMap<String, ArrayList<String>> buddies;

    public MessagingServer() {
        users = new HashMap<>();
        onlineUsers = new HashMap<>();
        String configFile = "resources/config.properties";
        Properties configs = Initialize.loadProperties(configFile);
        Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port"));
        Constants.RSA_CIPHER_TYPE = configs.getProperty("app.rsa.cipher.type");
        Constants.RSA_BLOCK_SIZE_ENCRYPT = Integer.parseInt(configs.getProperty("app.rsa.cipher.type.size.encrypt"));
        Constants.RSA_BLOCK_SIZE_DECRYPT = Integer.parseInt(configs.getProperty("app.rsa.cipher.type.size.decrypt"));
        Constants.AES_CIPHER_TYPE = configs.getProperty("app.aes.cipher.type");
        Constants.AES_BLOCK_SIZE_ENCRYPT = Integer.parseInt(configs.getProperty("app.aes.cipher.type.size.encrypt"));
        Constants.AES_BLOCK_SIZE_DECRYPT = Integer.parseInt(configs.getProperty("app.aes.cipher.type.size.decrypt"));
        Constants.CLIENT_KEYS_PATH = configs.getProperty("client.keys.path");
        Constants.PUBLIC_KEY_ALGO = configs.getProperty("app.publickey.algorithm");
        Constants.SECRET_KEY_ALGO = configs.getProperty("app.secretkey.algorithm");
        try {
            users = Initialize.getUsers(configs.getProperty("app.users.file"));
            buddies = Initialize.readBuddyList(configs.getProperty("app.buddy.list.file"));
            users.keySet().stream().forEach((user) -> {
                onlineUsers.put(user, null);
            });
            privateKey = Initialize.getPrivateKey(configs.getProperty("server.privatekey"),
                    Constants.PUBLIC_KEY_ALGO);
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Unable to instatiate server");
            System.exit(1);
        }
    }

    public void startServer() throws IOException, TamperedMessageException {
        System.out.println("Server started");
        while (true) {
            Socket socket = serverSocket.accept();
            new ThreadedServer(socket).start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private String authenticateClient(Socket socket) throws IOException, TamperedMessageException {
        String username = null;
        String finalMessage = this.receive(socket);
        AuthenticationMessage am = AuthenticationMessage.getObjectFromString(finalMessage);
        CommonUtility.verbose(am.toString(), true);
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
                InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
                System.out.println(am.getUsername() + " logged in from " + address.getAddress());
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
        CommonUtility.verbose(message, true);
        CommonUtility.verbose(finalMessage, true);
        out.write(finalMessage);
        return 0;
    }

    private String receive(Socket socket) throws IOException {
        String finalMessage = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        // This is a crime. I've hard coded the volume of buffer
        byte[] message = new byte[65536];
        int size = in.read(message);
        CommonUtility.verbose(Arrays.copyOfRange(message, 0, size), true);
        finalMessage = CommonUtility.decrypt(privateKey, message, size);
        CommonUtility.verbose(finalMessage, true);
        return finalMessage;
    }

    private ArrayList<Pair<String, String>> getBuddyList(String username) {
        ArrayList<String> buddy = buddies.get(username);
        ArrayList<Pair<String, String>> buddyList = new ArrayList<>();
        buddy.stream().map((string) -> {
            Pair<String, String> p = new Pair<>();
            p.setFirst(string);
            p.setSecond(null);
            if (onlineUsers.containsKey(string)) {
                if (onlineUsers.get(string) != null) {
                    p.setSecond(onlineUsers.get(string).getHostString());
                }
            }
            return p;
        }).forEach((p) -> {
            buddyList.add(p);
        });
        return buddyList;
    }

    private boolean authenticateOtwayRees(Socket socket) throws IOException, TamperedMessageException {
        boolean authenticate = false;
        byte[] payloadOne = new byte[65536];
        byte[] payloadTwo = new byte[65536];
        DataInputStream in = new DataInputStream(socket.getInputStream());
        int sizeOne = in.read(payloadOne);
        int sizeTwo = in.read(payloadTwo);
        CommonUtility.verbose(Arrays.copyOfRange(payloadOne, 0, sizeOne), true);
        CommonUtility.verbose(Arrays.copyOfRange(payloadTwo, 0, sizeTwo), true);
        String objectOne = CommonUtility.decrypt(privateKey, payloadOne, sizeOne);
        String objectTwo = CommonUtility.decrypt(privateKey, payloadTwo, sizeTwo);
        CommonUtility.verbose(objectOne, true);
        CommonUtility.verbose(objectTwo, true);
        FirstMessagePayload msgPayloadOne = FirstMessagePayload.getObjectFromString(objectOne);
        msgPayloadOne.verifyMessageHash();
        FirstMessagePayload msgPayloadTwo = FirstMessagePayload.getObjectFromString(objectTwo);
        msgPayloadTwo.verifyMessageHash();
        if (msgPayloadOne.getNc() == msgPayloadTwo.getNc()) {
            try {
                KeyGenerator keygen = KeyGenerator.getInstance(Constants.SECRET_KEY_ALGO);
                SecureRandom sr = new SecureRandom((msgPayloadOne.getSender() + msgPayloadOne.getReceiver()).getBytes());
                keygen.init(sr);
                SecretKey key = keygen.generateKey();
                String sender = msgPayloadOne.getSender();
                String receiver = msgPayloadOne.getReceiver();
                PublicKey sendersPublicKey = Initialize.getPublicKey(Constants.CLIENT_KEYS_PATH + sender + Constants.CLIENT_PUBLIC_KEY_SUFFIX, Constants.PUBLIC_KEY_ALGO);
                PublicKey receiversPublicKey = Initialize.getPublicKey(Constants.CLIENT_KEYS_PATH + receiver + Constants.CLIENT_PUBLIC_KEY_SUFFIX, Constants.PUBLIC_KEY_ALGO);
                SecondMessagePayload secondMessagePayloadOne = new SecondMessagePayload(msgPayloadOne.getNa(), key);
                secondMessagePayloadOne.insertMessageHash();
                CommonUtility.verbose(secondMessagePayloadOne.toString(), true);
                byte[] senderPayload = CommonUtility.encrypt(sendersPublicKey, secondMessagePayloadOne.toString());
                CommonUtility.verbose(senderPayload, true);
                SecondMessagePayload secondMessagePayloadTwo = new SecondMessagePayload(msgPayloadTwo.getNa(), key);
                secondMessagePayloadTwo.insertMessageHash();
                CommonUtility.verbose(secondMessagePayloadTwo.toString(), true);
                byte[] recieverPayload = CommonUtility.encrypt(receiversPublicKey, secondMessagePayloadTwo.toString());
                CommonUtility.verbose(recieverPayload, true);
                SecondMessage sm = new SecondMessage(msgPayloadOne.getNc(), senderPayload, recieverPayload);
                sm.insertMessageHash();
                CommonUtility.verbose(sm.toString(), true);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                byte[] str = sm.toString().getBytes();
                out.write(str);
                authenticate = true;
            } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return authenticate;
    }

    class ThreadedServer extends Thread {

        final Socket socket;

        public ThreadedServer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                String username = authenticateClient(socket);
                sendBuddyList(socket, username);
                authenticateOtwayRees(socket);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | TamperedMessageException ex) {
                Logger.getLogger(MessagingServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
