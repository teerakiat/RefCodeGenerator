package th.co.itmx.otp;

public class KeyManager {
    private String key = "YrLmCdNQ4OCZz23f2STHAYZnbwF8pWWd";

    public static KeyManager newInstance(){
        return new KeyManager();
    }

    /*
     *
     */
    public byte[] encrypt(byte[] plainText) throws Exception{
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
