package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class MessagingClient {

    private Socket socket;
    private ArrayList<String> buddyList;
    private PublicKey serversPublicKey;
    private PrivateKey privateKey;

    public MessagingClient() {
        String configFile = "resources/config.properties";
        Properties configs = Initialize.loadProperties(configFile);
        Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port"));
        Constants.SERVER_ADDRESS = configs.getProperty("server.address");
        Constants.CIPHER_TYPE = configs.getProperty("app.cipher.type");
        Constants.CLIENT_KEY_PREFIX = configs.getProperty("client.key.path");
        Constants.PUBLIC_KEY_ALGORITHM = configs.getProperty("app.algorithm");
        try {
            serversPublicKey = Initialize.getPublicKey(configs.getProperty("server.publickey"), configs.getProperty("app.algorithm"));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(MessagingClient.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Unable to instatiate client");
            System.exit(1);
        }
    }

    public void start() throws IOException {
        socket = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
        if (this.authenticateClient()) {
            //do rest of communication
            //buddyList = this.getBuddyList();
        } else {
            System.out.println("Exiting.");
            this.stop();
            System.exit(1);
        }
    }

    private void stop() throws IOException {
        socket.close();
    }

    private boolean authenticateClient() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Username: ");
        String username = br.readLine();
        System.out.print("Password: ");
        String password = br.readLine();
        AuthenticationMessage am = new AuthenticationMessage(username, password);
        am.insertTimestamp();
        am.calculateHash();
        String message = am.toString();
        try {
            int code = this.send(message);
            if (code < 1) {
                // Success
                System.out.println("Logged in!");
                this.privateKey = Initialize.getPrivateKey(
                        Constants.CLIENT_KEY_PREFIX + username + Constants.CLIENT_PRIVATE_KEY_SUFFIX,
                        Constants.PUBLIC_KEY_ALGORITHM);
                return true;
            } else {
                if (code < 2) {
                    System.out.println("Bad password.");
                } else if (code < 3) {
                    System.out.println("Bad username.");
                } else {
                    System.out.println("Authentication error");
                }
                return false;
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(MessagingClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(MessagingClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private int send(String message) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        DataOutputStream out;
        DataInputStream in;
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        byte[] finalMessage = this.getEncryptedMessage(message);
//        byte[] finalMessage = new byte[mess.length + ];
        out.write(finalMessage);
        int returnCode = in.readInt();
        in.close();
        out.close();
        return returnCode;
    }

    private byte[] getEncryptedMessage(String message) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, this.serversPublicKey);
        System.out.println(message);
        ArrayList<Byte> finalMessage = new ArrayList<>();
        int totalSize = message.length();
        System.out.println("total: " + totalSize);
        int blockSize = 213;
        for (int i = 0; i < message.length(); i += blockSize) {
            int size = blockSize;
            if (totalSize >= blockSize) {
                totalSize -= blockSize;
            } else {
                size = totalSize;
            }
            System.out.println("SIZE: " + size + "\tREM: " + totalSize);
            String substring = message.substring(i, i + size);
            System.out.println("i = " + i + " || SUB: " + substring);
            byte[] cipherr = cipher.doFinal(substring.getBytes());
            for (byte b : cipherr) {
                finalMessage.add(b);
            }
            //totalSize += size;
        }
        byte[] mess = new byte[finalMessage.size()];
        int i = 0;
        for (Byte b : finalMessage) {
            mess[i++] = b;
        }
        return mess;
    }

    private ArrayList<String> getBuddyList() {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
