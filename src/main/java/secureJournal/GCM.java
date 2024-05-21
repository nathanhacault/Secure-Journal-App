package secureJournal;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class GCM { // Class used for encryption and decryption operations.

    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA512";
    private static final int TAG_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 32;
    private static final int ITERATIONS = 65535;

    // Function to derive AES key from password and salt.
    private static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH * 8);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    // Function to decrypt the cipher content.
    public static String decrypt(String cipherContent, String password) throws Exception {
        // Decode Base64 encoded string
        byte[] decode = Base64.getDecoder().decode(cipherContent.getBytes(UTF_8));
        ByteBuffer byteBuffer = ByteBuffer.wrap(decode);

        // Extract salt, IV, and content from the byte buffer
        byte[] salt = new byte[SALT_LENGTH];
        byteBuffer.get(salt);

        byte[] iv = new byte[IV_LENGTH];
        byteBuffer.get(iv);

        byte[] content = new byte[byteBuffer.remaining()];
        byteBuffer.get(content);

        // Initialize cipher for decryption
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        SecretKey aesKeyFromPassword = getAESKeyFromPassword(password.toCharArray(), salt);
        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH * 8, iv));

        // Perform decryption and return plain text
        byte[] plainText = cipher.doFinal(content);
        return new String(plainText, UTF_8);
    }

    // Function to encrypt the plain message.
    public static String encrypt(String password, String plainMessage) throws Exception {
        // Generate salt and secret key
        byte[] salt = getRandomNonce(SALT_LENGTH);
        SecretKey secretKey = getSecretKey(password, salt);

        // Generate IV and initialize cipher for encryption
        byte[] iv = getRandomNonce(IV_LENGTH);
        Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

        // Encrypt the plain message
        byte[] encryptedMessageByte = cipher.doFinal(plainMessage.getBytes(UTF_8));

        // Combine salt, IV, and encrypted message into a single byte array
        byte[] cipherByte = ByteBuffer.allocate(salt.length + iv.length + encryptedMessageByte.length)
                .put(salt)
                .put(iv)
                .put(encryptedMessageByte)
                .array();

        // Return Base64 encoded cipher text
        return Base64.getEncoder().encodeToString(cipherByte);
    }

    // Function to generate a random nonce of the specified length.
    public static byte[] getRandomNonce(int length) {
        byte[] nonce = new byte[length];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    // Function to derive a secret key from the provided password and salt.
    public static SecretKey getSecretKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Define the key specification using the provided password, salt, iteration count, and key length.
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH * 8);

        // Generate a secret key factory instance using the specified algorithm.
        SecretKeyFactory factory = SecretKeyFactory.getInstance(FACTORY_INSTANCE);

        // Generate and return a secret key using the generated key specification.
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    // Function to initialize the cipher with the specified mode, secret key, and initialization vector (IV).
    private static Cipher initCipher(int mode, SecretKey secretKey, byte[] iv) throws InvalidKeyException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException {
        // Instantiate a cipher instance using the specified algorithm.
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // Initialize the cipher with the provided mode, secret key, and IV.
        cipher.init(mode, secretKey, new GCMParameterSpec(TAG_LENGTH * 8, iv));

        // Return the initialized cipher.
        return cipher;
    }
}
