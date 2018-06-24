package th.co.itmx;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import th.co.itmx.otp.KeyManager;
import th.co.itmx.otp.OTPManager;
import th.co.itmx.otp.exception.InvalidFormatException;
import th.co.itmx.util.Util;

import javax.xml.bind.DatatypeConverter;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static th.co.itmx.util.Util.byteArrayToHexString;


public class Main {


    //nodejs: Buffer.from('74656572616b696174636869afabacad', 'utf8').toString('hex');
    // 5123456789abcdef1234567890abcdef
    //java: Integer.toHexString(Integer.parseInt(hex));
//    private static String inputKey(String questionText) throws Exception{
//        char secretKey[];
//        String confirm="n";
//        int minimumKeyLength = 32; //having at least 32 Hexadecimal
//        do {
//            System.out.println("Please enter your key in Hexadecimal, supported char [0-9][A-F][a-f], input at least "+minimumKeyLength+" chars");
//            Console console = System.console();
//            secretKey = console.readPassword(questionText);
//            String secretKeyStr = new String(secretKey);
//
//            if (!secretKeyStr.matches("^[0-9a-fA-F]{"+minimumKeyLength+",}$")) {
//                System.out.println("invalid key format, please enter your input again");
//                continue;
//            }
//
//            System.out.println("Confirm following key is correct??: " + secretKeyStr);
//            System.out.print("[y/n]");
//            confirm = console.readLine();
//
//        }while(!confirm.equalsIgnoreCase("y") );
//        Util.clearScreen();
//        return new String(secretKey);
//    }

    private static byte[] generateRandomKey () throws Exception{
        byte[] bytes = new byte[32];
        SecureRandom.getInstanceStrong().nextBytes(bytes);
        return bytes;
    }

    private static void generateEncryptedKey(String itmx_pub_file, String telco_pub_file, String outputFolder) throws Exception {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] pubKeyBytes = Files.readAllBytes(new File(itmx_pub_file).toPath());
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubKeyBytes);
            PublicKey itmxPub = kf.generatePublic(pubSpec);

            pubKeyBytes = Files.readAllBytes(new File(telco_pub_file).toPath());
            pubSpec = new X509EncodedKeySpec(pubKeyBytes);
            PublicKey telcoPub = kf.generatePublic(pubSpec);

            byte[] shareKey = generateRandomKey();
            //encryption
            KeyManager manager = new KeyManager();
            byte[] itmxEncryptedKey = manager.encrypt(shareKey, itmxPub);
            byte[] telcoEncryptedKey = manager.encrypt(shareKey, telcoPub);

            //********** for testing only
//            System.out.println("encrypt key: "+ byteArrayToHexString(shareKey));

            //create output folder if not exist
            new File(outputFolder).mkdir();

            //write encryption to file
            String itmxOutputFile = outputFolder + "\\itmx_key.enc";
            String telcoOutputFile = outputFolder + "\\telco_key.enc";

            try (FileOutputStream fos = new FileOutputStream(itmxOutputFile)) {
                fos.write(itmxEncryptedKey);
                fos.close();
            }
            try (FileOutputStream fos = new FileOutputStream(telcoOutputFile)) {
                fos.write(telcoEncryptedKey);
                fos.close();
            }

            //********* decrypt, for testing only
//            System.out.println("encrypted result : " + byteArrayToHexString(itmxEncryptedKey));
//            byte[] priKeyBytes = Files.readAllBytes(new File("D:\\temp\\itmx_private_key.der").toPath());
//            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(priKeyBytes);
//            PrivateKey pri = kf.generatePrivate(spec);
//
//            byte[] resultByte = manager.decrypt(itmxEncryptedKey, pri);
//            System.out.println("result : " + byteArrayToHexString(resultByte));
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    private static void validateNotNull(String value, String varName) throws InvalidFormatException{
        if (value == null) {
            throw new InvalidFormatException(varName + " is mandatory");
        }
    }

    public static void main(String[] args) {
        String commandList = "gen, otp";
        Options options = new Options();

        Option command = new Option("c", "command", true, "execution command, possible value : "+ commandList);
        command.setRequired(true);
        options.addOption(command);

        //require only for gen command
        Option option_itmx_pub_key_path = new Option("f", "itmx_pub", true, "itmx public file for encrypted output");
        option_itmx_pub_key_path.setRequired(false);
        options.addOption(option_itmx_pub_key_path);

        Option option_telco_pub_key_path = new Option("t", "telco_pub", true, "telco public file for encrypted output");
        option_telco_pub_key_path.setRequired(false);
        options.addOption(option_telco_pub_key_path);

        Option option_output_key_path = new Option("o", "output", true, "output encrypted share key folder path");
        option_output_key_path.setRequired(false);
        options.addOption(option_output_key_path);

        //Require only for testing otp
        Option opton_pri_key_path = new Option("p", "pri_key", true, "private key for testing otp");
        opton_pri_key_path.setRequired(false);
        options.addOption(opton_pri_key_path);

        Option option_request_date = new Option("d", "request_date", true, "requested date for testing otp ex. 20180621");
        option_request_date.setRequired(false);
        options.addOption(option_request_date);

        Option option_encrypted_key = new Option("e", "encrypted_key", true, "encrypted key path for testing otp");
        option_encrypted_key.setRequired(false);
        options.addOption(option_encrypted_key);

        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLineParser parser = new DefaultParser();

            CommandLine cmd = parser.parse(options, args);

            String commandValue = cmd.getOptionValue("command");

            if (commandValue.equalsIgnoreCase("gen")) {
                String itmx_pub_file = cmd.getOptionValue("itmx_pub");
                String telco_pub_file = cmd.getOptionValue("telco_pub");
                String output_folder = cmd.getOptionValue("output");

                validateNotNull(itmx_pub_file, "itmx public key file");
                validateNotNull(telco_pub_file, "telco public key file");
                if(output_folder == null){
                    Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
                    output_folder = path.toString() + "/output";
                }
                System.out.println("itmx public key file path: "+itmx_pub_file);
                System.out.println("telco public key file path: "+telco_pub_file);
                System.out.println("output folder: "+output_folder);

                generateEncryptedKey(itmx_pub_file, telco_pub_file, output_folder);

            } else if (commandValue.equalsIgnoreCase("otp")) {
                String private_key_path = cmd.getOptionValue("pri_key");
                String requested_date = cmd.getOptionValue("request_date");
                String encrypted_key = cmd.getOptionValue("encrypted_key");
                validateNotNull(private_key_path, "private key file path");
                validateNotNull(encrypted_key, "encrypted share secret key file path");
                validateNotNull(requested_date, "requested date ex. 20180621");

                // Generate Private Key
                byte[] priKeyBytes = Files.readAllBytes(new File(private_key_path).toPath());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(priKeyBytes);
                PrivateKey pri = kf.generatePrivate(spec);

                // Decrypt seed
                byte[] encryptedSeed = Files.readAllBytes(Paths.get(encrypted_key));
                byte[] seed = KeyManager.newInstance().decrypt(encryptedSeed, pri);

                System.out.println("Verifying reference code....");
                generateRefCode(seed, requested_date);

            }else{
                System.out.println("Incorrect command code provide, possible command code are: "+commandList);
                System.exit(1);
            }
        } catch (ParseException ex) {
            formatter.printHelp("mobile-security", options);
            System.out.println(ex.getMessage());
            System.exit(1);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

    private static void generateRefCode(byte[] seed, String interval) throws Exception {

        Console console = System.console();

        String nationalId = console.readLine("Enter national Id:");
        String mobile = console.readLine("Enter mobile number:");

        OTPManager manager = OTPManager.newInstance();
        String otp = manager.generateRefCode(seed, interval, nationalId, mobile);
        System.out.println(otp);
    }
}
