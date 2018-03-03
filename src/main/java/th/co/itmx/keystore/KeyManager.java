package th.co.itmx.keystore;


import org.apache.commons.lang3.StringUtils;
import th.co.itmx.util.AdvancedEncryptionStandard;
import th.co.itmx.util.InvalidFormatException;
import th.co.itmx.util.Util;

import java.io.Console;
import java.io.FileOutputStream;
import java.math.BigInteger;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

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

        AdvancedEncryptionStandard advancedEncryptionStandard = new AdvancedEncryptionStandard(
                encryptionKey);
        return advancedEncryptionStandard.encrypt(plainText);
    }

    public byte[] decrypt(byte[] encryptKey) throws Exception{
        byte[] encryptionKey = key.getBytes(UTF_8);
        AdvancedEncryptionStandard advancedEncryptionStandard = new AdvancedEncryptionStandard(
                encryptionKey);
        return advancedEncryptionStandard.decrypt(encryptKey);
    }


    //nodejs: Buffer.from('74656572616b696174636869', 'utf8').toString('hex');
    //java: Integer.toHexString(Integer.parseInt(hex));
    private String inputKey(String questionText) throws Exception{
        char secretKey[];
        String confirm="n";
        int minimumKeyLength = 24; //having at least 24 Hexadecimal
        do {
            System.out.println("Please enter your key in Hexadecimal, supported char [0-9][A-F][a-f], input at least "+minimumKeyLength+" chars");
            Console console = System.console();
            secretKey = console.readPassword(questionText);
            String secretKeyStr = new String(secretKey);

            if (!secretKeyStr.matches("^[0-9a-fA-F]{"+minimumKeyLength+",}$")) {
                System.out.println("invalid key format, please enter your input again");
                continue;
            }

            System.out.println("Confirm following key is correct??: " + secretKeyStr);
            System.out.print("[y/n]");
            confirm = console.readLine();

        }while(!confirm.equalsIgnoreCase("y") );
        Util.clearScreen();
        return new String(secretKey);
    }

    public void generateKey(String outputFile) throws Exception {
        try {
            if(StringUtils.isEmpty(outputFile)){
                throw new InvalidFormatException("Output file is mandatory, please provide output file");
            }

            //create input bytes, full key compose of key of two components
            // 1: itmx key, have at least 24 hexadecimal chars
            // 2: telco key, have at least 24 hexadecimal chars
            String itmxSecretKey = inputKey("Enter ITMX Secret Key: ");
            String telcoSecretKey = inputKey("Enter Telco Secret Key: ");

            //combine two key and convert to byte array
            String fullKey = itmxSecretKey + telcoSecretKey;
            byte[] byteKey = new BigInteger(fullKey,16).toByteArray();
//            System.out.println(byteArrayToHexString(byteKey));

            //encryption
            byte[] encryptedKey = encrypt(byteKey);
//            System.out.println("encrypt key: "+ byteArrayToHexString(encryptedKey));
            //write encryption to file
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(encryptedKey);
                fos.close();
            }

            //decrypt
//            byte[] resultByte = manager.decrypt(encryptedKey);
//            System.out.println("result : " + byteArrayToHexString(resultByte));
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}
