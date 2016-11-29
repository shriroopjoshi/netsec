package client;

import exceptions.TamperedMessageException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import messages.AuthenticationMessage;
import messages.BuddylistMessage;
import utility.CommonUtility;
import utility.Constants;
import utility.Initialize;
import utility.Pair;

/**
 *
 * @author shriroop
 */
public class ServerLoginClient {

    private Socket socket;
    private PrivateKey privateKey;
    private PublicKey serversPublicKey;
    private ArrayList<Pair<String, String>> buddyList;
    private String username;

    public ServerLoginClient() {
        String configFile = "resources/config.properties";
        Properties configs = Initialize.loadProperties(configFile);
        Constants.SERVER_PORT = Integer.parseInt(configs.getProperty("server.port"));
        Constants.SERVER_ADDRESS = configs.getProperty("server.address");
        Constants.CIPHER_TYPE = configs.getProperty("app.cipher.type");
        Constants.PUBLIC_KEY_ALGO = configs.getProperty("app.publickey.algorithm");
        Constants.CLIENT_KEYS_PATH = configs.getProperty("client.keys.path");
        Constants.CLIENT_PORT = Integer.parseInt(configs.getProperty("client.port"));
        try {
            serversPublicKey = Initialize.getPublicKey(configs.getProperty("server.publickey"), Constants.PUBLIC_KEY_ALGO);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(ServerLoginClient.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Unable to instatiate client");
            System.exit(1);
        }
        username = null;
    }

    public void start() throws IOException, TamperedMessageException {
        System.out.println("MessagingApp started");
        System.out.println("Connecting to server - " + Constants.SERVER_ADDRESS);
        socket = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
        if (this.authenticateClient()) {
            this.populateBuddyList();
            // TODO: Launch the server socket to listen for all incoming message
            if (this.buddyList == null) {
                System.out.println("Awww. You're so lonely!");
                System.out.println("Exiting\n");
                /**
                 * Space to implement LOGOUT functionality
                 */
                //this.logout();
                System.exit(0);
            }
            ServerSocket client = new ServerSocket(Constants.CLIENT_PORT);
            ClientService clientService = new ClientService(client, this.username, this.serversPublicKey, this.privateKey, this.socket);
            clientService.start();
            System.out.println("Buddy list:");
            for (int i = 0; i < buddyList.size(); i++) {
                System.out.print((i + 1) + ". " + buddyList.get(i).getFirst());
                if (buddyList.get(i).getSecond() != null) {
                    System.out.println(" (online)");
                } else {
                    System.out.println("");
                }
            }
            System.out.println("[:q!] to exit");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = br.readLine();
                if (line.equals(":q!")) {
                    //this.logout();
                    System.exit(0);
                }
                int index = Integer.parseInt(line) - 1;
                System.out.println("Selected index: " + buddyList.get(index).getFirst());
                System.out.println("Selected index: " + buddyList.get(index).getSecond().toString());
                AuthenticationClient ac = new AuthenticationClient(serversPublicKey, privateKey, buddyList.get(index), username);
                ac.authenticate();
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
        int code = this.send(message);
        if (code < 1) {
            // Success
            System.out.println("Welcome " + am.getUsername() + "!");
            this.username = am.getUsername();
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
    }

    private int send(String message) throws IOException {
        DataOutputStream out;
        DataInputStream in;
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        byte[] finalMessage = CommonUtility.encrypt(serversPublicKey, message);
        out.write(finalMessage);
        int returnCode = in.readInt();
        return returnCode;
    }

    private void readPrivateKey(String username) {
        try {
            this.privateKey = Initialize.getPrivateKey(Constants.CLIENT_KEYS_PATH + username + Constants.CLIENT_PRIVATE_KEY_SUFFIX, Constants.PUBLIC_KEY_ALGO);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(ServerLoginClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void populateBuddyList() throws TamperedMessageException {
        String message = null;
        try {
            message = this.receive();
        } catch (IOException ex) {
            Logger.getLogger(ServerLoginClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        BuddylistMessage list = BuddylistMessage.getObjectFromString(message);
        if (list.verifyMessageHash()) {
            this.buddyList = list.getBuddyList();
        } else {
            this.buddyList = null;
            throw new TamperedMessageException();
        }
    }

    private String receive() throws IOException {
        String finalMessage = null;
        DataInputStream in = new DataInputStream(socket.getInputStream());
        // This is a crime. I've hard coded the volume of buffer
        byte[] message = new byte[4096];
        int size = in.read(message);
        finalMessage = CommonUtility.decrypt(privateKey, message, size);
        return finalMessage;
    }

    private void logout() {
        try {
            int ret = this.send("logout");
        } catch (IOException ex) {
            Logger.getLogger(ServerLoginClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
