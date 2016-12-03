package utility;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import messages.otwayrees.FirstMessagePayload;

/**
 *
 * @author shriroop
 */
public class CommonUtility {

    public static byte[] encrypt(PublicKey publicKey, String message) {
        byte[] finalMessage = null;
        try {
            Cipher cipher = Cipher.getInstance(Constants.RSA_CIPHER_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int blockSize = Constants.RSA_BLOCK_SIZE_ENCRYPT;
            byte[] bytes = message.getBytes();
            int sizeMultiplier = bytes.length / blockSize;
            if (bytes.length % blockSize != 0) {
                sizeMultiplier += 1;
            }
            finalMessage = new byte[sizeMultiplier * Constants.RSA_BLOCK_SIZE_DECRYPT];
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
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(FirstMessagePayload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalMessage;
    }

    public static byte[] encrypt(SecretKey secretKey, String message) {
        byte[] finalMessage = null;
        try {
            Cipher cipher = Cipher.getInstance(Constants.AES_CIPHER_TYPE);
            IvParameterSpec ivSpec = new IvParameterSpec(Constants.IV);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] bytes = message.getBytes();
            int blockSize = Constants.AES_BLOCK_SIZE_ENCRYPT;
            int sizeMultiplier = bytes.length / blockSize;
            if (bytes.length % blockSize != 0) {
                sizeMultiplier += 1;
            }
            finalMessage = new byte[sizeMultiplier * Constants.AES_BLOCK_SIZE_DECRYPT];
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
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(CommonUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalMessage;
    }

    public static String decrypt(PrivateKey privateKey, byte[] message, int size) {
        String finalMessage = "";
        try {
            Cipher cipher = Cipher.getInstance(Constants.RSA_CIPHER_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int blockSize = Constants.RSA_BLOCK_SIZE_DECRYPT;
            int iteration = 1;
            for (int i = 0; i < size; i += blockSize) {
                int min = Math.min(message.length, blockSize * iteration);
                finalMessage += new String(cipher.doFinal(Arrays.copyOfRange(message, i, min)));
                iteration += 1;
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(FirstMessagePayload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalMessage;
    }

    public static String decrypt(SecretKey secretKey, byte[] message, int size) {
        String finalMessage = "";
        try {
            Cipher cipher = Cipher.getInstance(Constants.AES_CIPHER_TYPE);
            IvParameterSpec ivSpec = new IvParameterSpec(Constants.IV);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            int blockSize = Constants.RSA_BLOCK_SIZE_DECRYPT;
            int iteration = 1;
            for (int i = 0; i < size; i += blockSize) {
                int min = Math.min(message.length, blockSize * iteration);
                finalMessage += new String(cipher.doFinal(Arrays.copyOfRange(message, i, min)));
                iteration += 1;
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException |
                NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(FirstMessagePayload.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(CommonUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalMessage;
    }
}
