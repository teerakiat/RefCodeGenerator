package th.co.itmx.otp;

import th.co.itmx.otp.exception.InvalidFormatException;


public class KeyManager {
    private String key = "YrLmCdNQ4OCZz23f2STHAYZnbwF8pWWd";

    public static KeyManager newInstance(){
        return new KeyManager();
    }

    /*
     *
     */
    public byte[] encrypt(byte[] plainText) throws Exception{
        if(plainText.length < 24){
            throw new InvalidFormatException("invalid key length");
        }

        byte[] encryptionKey = key.getBytes();

//        System.out.println(encryptionKey.length);

        AesManager advancedEncryptionStandard = new AesManager(
                encryptionKey);
        return advancedEncryptionStandard.encrypt(plainText);
    }

    public byte[] decrypt(byte[] encryptKey) throws Exception{
        byte[] encryptionKey = key.getBytes(

        );
        AesManager advancedEncryptionStandard = new AesManager(
                encryptionKey);
        return advancedEncryptionStandard.decrypt(encryptKey);
    }

}
