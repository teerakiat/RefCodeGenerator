package th.co.itmx;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import th.co.itmx.otp.KeyManager;
import th.co.itmx.otp.OTPManager;
import th.co.itmx.otp.exception.InvalidFormatException;
import th.co.itmx.util.Util;

import java.io.Console;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Main {


    //nodejs: Buffer.from('74656572616b696174636869', 'utf8').toString('hex');
    //java: Integer.toHexString(Integer.parseInt(hex));
    private static String inputKey(String questionText) throws Exception{
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

    private static void generateKey(String outputFile) throws Exception {
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
            KeyManager manager = new KeyManager();
            byte[] encryptedKey = manager.encrypt(byteKey);
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

    public static void main(String[] args) {
        Options options = new Options();

        Option command = new Option("c", "command", true, "execution command, possible value : genkey, ref_code");
        command.setRequired(true);
        options.addOption(command);

        Option keyfile = new Option("f", "keyfile", true, "keyfile output in case of genkey, keyfile as input in case of otp");
        keyfile.setRequired(false);
        options.addOption(keyfile);

        Option interval = new Option("t", "time", true, "time in seconds of the otp to be expire");
        interval.setRequired(false);
        options.addOption(interval);

        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLineParser parser = new DefaultParser();

            CommandLine cmd = parser.parse(options, args);

            String commandValue = cmd.getOptionValue("command");

            if (commandValue.equalsIgnoreCase("genkey")) {
                String outputFile = cmd.getOptionValue("keyfile");
                generateKey(outputFile);
            } else if (commandValue.equalsIgnoreCase("otp")) {
                String keyFilePath = cmd.getOptionValue("keyfile");
                String intervalValue = cmd.getOptionValue("time");
                if (intervalValue == null) {
                    throw new InvalidFormatException("Expiry time is mandatory");
                }

                byte[] encryptedSeed = Files.readAllBytes(Paths.get(keyFilePath));
                byte[] seed = KeyManager.newInstance().decrypt(encryptedSeed);
                VerifyRefCode(seed, Integer.parseInt(intervalValue));

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

    private static void VerifyRefCode(byte[] seed, int interval) throws Exception {

        Console console = System.console();

        String nationalId = console.readLine("Enter national Id:");
        String mobile = console.readLine("Enter mobile number:");

        OTPManager manager = OTPManager.newInstance();
        manager.getPossibleRefCode(seed, interval, nationalId, mobile);
    }
}
