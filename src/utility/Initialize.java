package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author shriroop
 */
public class Initialize {

    /**
     * Loads properties from given configuration file
     *
     * @param filename path to configuration file
     * @return prop
     */
    public static Properties loadProperties(String filename) {
        InputStream configs;
        Properties prop = new Properties();
        try {
            configs = new FileInputStream(filename);
            prop.load(configs);
        } catch (FileNotFoundException ex) {
            System.err.println("CONFIG file not found\n" + ex);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Unable to read CONFIG file");
            System.exit(1);
        }
        return prop;
    }

    /**
     * Reads private key from a file in DER format
     *
     * @param filename path to input file
     * @param algorithm
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKey(String filename, String algorithm) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKey = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(spec);
    }

    public static PublicKey getPublicKey(String filename, String algorithm) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKey = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePublic(spec);
    }

    public static HashMap<String, String> getUsers(String filename) throws FileNotFoundException, IOException {
        HashMap<String, String> users = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            users = new HashMap<>();
            String line = br.readLine();
            while (line != null) {
                if (!line.startsWith("#")) {
                    String[] user = line.split("=");
                    users.put(user[0], user[1]);
                }
                line = br.readLine();
            }
        }
        return users;
    }

    public static HashMap<String, ArrayList<String>> readBuddyList(String filename) throws FileNotFoundException, IOException {
        HashMap<String, ArrayList<String>> buddyList = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = br.readLine();
        while (line != null) {
            String[] split = line.split("=");
            ArrayList<String> arr;
            if (split.length == 2) {
                arr = new ArrayList<>(Arrays.asList(","));
            } else {
                arr = null;
            }
            buddyList.put(split[0], arr);
            line = br.readLine();
        }
        br.close();
        return buddyList;
    }
}
