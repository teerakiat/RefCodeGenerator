package th.co.itmx.otp;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

class AesManager {
    private byte[] key;

    private static final String ALGORITHM = "AES";
    private static final String PADDING = ALGORITHM + "/CBC/PKCS5Padding";

    AesManager(byte[] key) {
        this.key = key;
    }

    /**
     * Encrypts the given plain text
     *
     * @param plainText The plain text to encrypt
     */
    byte[] encrypt(byte[] plainText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(PADDING);

        byte[] iv = new byte[cipher.getBlockSize()];
        final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        rnd.nextBytes(iv);
        AlgorithmParameterSpec spec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] encryptedData = cipher.doFinal(plainText);
        final byte[] ivAndEncryptedMessage = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, ivAndEncryptedMessage, 0, cipher.getBlockSize());
        System.arraycopy(encryptedData, 0, ivAndEncryptedMessage, cipher.getBlockSize(), encryptedData.length);

        return ivAndEncryptedMessage;
    }

    /**
     * Decrypts the given byte array
     *
     * @param cipherText The data to decrypt
     */
    byte[] decrypt(byte[] cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance(PADDING);
        int ivSize = cipher.getBlockSize();

        //extract iv
        byte[] iv = new byte[ivSize];
        System.arraycopy(cipherText, 0, iv, 0, iv.length);
        AlgorithmParameterSpec spec = new IvParameterSpec(iv);

        //extract encrypted bytes
        int encryptedSize = cipherText.length - ivSize;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(cipherText, ivSize, encryptedBytes, 0, encryptedSize);

        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        return cipher.doFinal(encryptedBytes);
    }
}