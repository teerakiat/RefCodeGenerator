package th.co.itmx.otp;

import th.co.itmx.otp.exception.InvalidFormatException;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyManager {
    private String key = "YrLmCdNQ4OCZz23f2STHAYZnbwF8pWWd";
//    private String rsa = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";
    private String rsa = "RSA/ECB/PKCS1Padding";

    public static KeyManager newInstance(){
        return new KeyManager();
    }

    /*
     *
     */
    public byte[] encrypt(byte[] plainText) throws Exception {
        byte[] encryptionKey = key.getBytes();
        //        System.out.println(encryptionKey.length);
        AesManager advancedEncryptionStandard = new AesManager(
                encryptionKey);
        return advancedEncryptionStandard.encrypt(plainText);
    }

    public byte[] encrypt(byte[] plainText, PublicKey publicKey) throws Exception{
        Cipher cipher = Cipher.getInstance(rsa);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedMsg = cipher.doFinal(plainText);
        return  encryptedMsg;
    }

    public byte[] decrypt(byte[] cipherText) throws Exception{
        byte[] encryptionKey = key.getBytes(

        );
        AesManager advancedEncryptionStandard = new AesManager(
                encryptionKey);
        return advancedEncryptionStandard.decrypt(cipherText);
    }

    public byte[] decrypt(byte[] cipherText, PrivateKey privateKey) throws Exception{

        Cipher decriptCipher = Cipher.getInstance(rsa);
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return decriptCipher.doFinal(cipherText);
    }
}
