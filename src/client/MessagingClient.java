package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
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
    private PublicKey serversPublicKey;

    public MessagingClient() {
        String configFile = "resources/config.properties";
        Properties configs = Initialize.loadProperties(configFile);
        Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port"));
        Constants.SERVER_ADDRESS = configs.getProperty("server.address");
        Constants.CIPHER_TYPE = configs.getProperty("app.cipher.type");
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
                System.out.println("Logged in!");
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
        System.out.println("MESS: " + message);
        byte[] bytes = message.getBytes();
        int sizeMultiplier = bytes.length / blockSize;
        if(bytes.length % blockSize != 0)
            sizeMultiplier += 1;
        byte[] finalMessage = new byte[sizeMultiplier * 256];
        System.out.println("qwer: " + finalMessage.length);
        int counter = 0;
        System.out.println("SIZE: " + bytes.length);
        int iteration = 1;
        for (int i = 0; i < bytes.length; i += blockSize) {
            int min = Math.min(bytes.length, blockSize * iteration);
            System.out.println("Block till " + min);
            byte[] block = Arrays.copyOfRange(bytes, i, min);
            byte[] enc = cipher.doFinal(block);
            System.out.println("ENC: " + enc.length);
            for (byte b : enc) {
                finalMessage[counter] = b;
                counter += 1;
            }
            iteration += 1;
        }
        System.out.println("LEN: " + finalMessage.length);
        out.write(finalMessage);
        int returnCode = in.readInt();
        in.close();
        out.close();
        return returnCode;
    }
}
