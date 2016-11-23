package client;

import exceptions.TamperedMessageException;
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
import messages.BuddylistMessage;
import utility.Constants;
import utility.Initialize;

/**
 *
 * @author shriroop
 */
public class MessagingClient {

    private Socket socket;
    private PrivateKey privateKey;
    private PublicKey serversPublicKey;
    private ArrayList<String> buddyList;

    public MessagingClient() {
        String configFile = "resources/config.properties";
        Properties configs = Initialize.loadProperties(configFile);
        Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port"));
        Constants.SERVER_ADDRESS = configs.getProperty("server.address");
        Constants.CIPHER_TYPE = configs.getProperty("app.cipher.type");
        Constants.PUBLIC_KEY_ALGO = configs.getProperty("app.algorithm");
        Constants.CLIENT_KEYS_PATH = configs.getProperty("client.keys.path");
        try {
            serversPublicKey = Initialize.getPublicKey(configs.getProperty("server.publickey"), Constants.PUBLIC_KEY_ALGO);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(MessagingClient.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Unable to instatiate client");
            System.exit(1);
        }
    }

    public void start() throws IOException, TamperedMessageException {
        socket = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
        if (this.authenticateClient()) {
            //do rest of communication
            this.populateBuddyList();
            System.out.println("Buddy list:");
            for (int i = 0; i < buddyList.size(); i++) {
                System.out.println((i + 1) + ". " + buddyList.get(i));
            }
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
        am.insertMessageHash();
        String message = am.toString();
        try {
            int code = this.send(message);
            if (code < 1) {
                // Success
                System.out.println("Welcome " + am.getUsername() + "!");
                this.readPrivateKey(am.getUsername());
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
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, this.serversPublicKey);
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
        int returnCode = in.readInt();
        return returnCode;
    }

    private void readPrivateKey(String username) {
        try {
            this.privateKey = Initialize.getPrivateKey(Constants.CLIENT_KEYS_PATH + username + Constants.CLIENT_PRIVATE_KEY_SUFFIX, Constants.PUBLIC_KEY_ALGO);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(MessagingClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void populateBuddyList() throws TamperedMessageException {
        String message = null;
        try {
            message = this.receive();
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | IOException ex) {
            Logger.getLogger(MessagingClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        BuddylistMessage list = BuddylistMessage.getObjectFromString(message);
        if (list.verifyMessageHash()) {
            this.buddyList = list.getBuddyList();
        } else {
            this.buddyList = null;
            throw new TamperedMessageException();
        }
    }

    private String receive() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException {
        String finalMessage = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        // This is a crime. I've hard coded the volume of buffer
        byte[] message = new byte[4096];
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
}
