package th.co.itmx.otp;

import org.apache.commons.lang3.time.DateUtils;
import th.co.itmx.otp.exception.InvalidFormatException;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


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

    private byte[] encrypt(byte[] plainText, PublicKey publicKey) throws Exception{
        Cipher cipher = Cipher.getInstance(rsa);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedMsg = cipher.doFinal(plainText);
        return  encryptedMsg;
    }

    /**
     *
     * @param plainText: raw text as an input for encyption
     * @param publicKey: public key use for encrypt
     * @param expireYear: this encrypted will be expire in??
     * @return
     * @throws Exception
     */
    public byte[] encrypt(byte[] plainText, PublicKey publicKey, int expireYear) throws Exception {
        byte[] encryptedMsg = encrypt(plainText, publicKey);

        /* get current day time zone */
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long startDateEpoch = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, expireYear);
        long expireDateEpoch = cal.getTimeInMillis();

        ByteBuffer buffer = ByteBuffer.allocate(encryptedMsg.length + 2 + (Long.BYTES*2));
        buffer.put(encryptedMsg);
        buffer.put(new byte[] {'|'});
        buffer.putLong(startDateEpoch);
        buffer.put(new byte[] {'|'});
        buffer.putLong(expireDateEpoch);

        return buffer.array();
    }

    public Date getEndDate(byte[] encryptedText){
        byte[] b_expireDateEpoch = new byte[Long.BYTES];

        System.arraycopy(encryptedText, encryptedText.length-Long.BYTES, b_expireDateEpoch, 0, Long.BYTES);
        long l_expireDate = ByteUtils.bytesToLong(b_expireDateEpoch);

        return new Date(l_expireDate);
    }

    public Date getStartDate(byte[] encryptedText){
        byte[] b_startDateEpoch = new byte[Long.BYTES];

        System.arraycopy(encryptedText, encryptedText.length-(Long.BYTES*2)-1, b_startDateEpoch, 0, Long.BYTES);
        long l_startDate = ByteUtils.bytesToLong(b_startDateEpoch);

        return new Date(l_startDate);
    }

    public byte[] decrypt(byte[] cipherText) throws Exception{
        byte[] encryptionKey = key.getBytes();
        AesManager advancedEncryptionStandard = new AesManager(
                encryptionKey);
        return advancedEncryptionStandard.decrypt(cipherText);
    }

    public byte[] decrypt(byte[] originalCipherText, PrivateKey privateKey) throws Exception{

        Cipher decriptCipher = Cipher.getInstance(rsa);
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedText = new byte[Long.BYTES];
        System.arraycopy(originalCipherText, 0, encryptedText, 0, originalCipherText.length-(Long.BYTES*2)-2);

        return decriptCipher.doFinal(encryptedText);
    }


}
