package th.co.itmx;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import th.co.itmx.otp.KeyManager;
import th.co.itmx.otp.OTPManager;
import th.co.itmx.otp.exception.InvalidFormatException;
import th.co.itmx.util.Util;

import javax.xml.bind.DatatypeConverter;
import java.io.Console;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Main {


    //nodejs: Buffer.from('74656572616b696174636869afabacad', 'utf8').toString('hex');
    // 5123456789abcdef1234567890abcdef
    //java: Integer.toHexString(Integer.parseInt(hex));
    private static String inputKey(String questionText) throws Exception{
        char secretKey[];
        String confirm="n";
        int minimumKeyLength = 32; //having at least 32 Hexadecimal
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
            // 1: itmx key
            // 2: telco key
            String itmxSecretKey = inputKey("Enter ITMX Secret Key: ");
            String telcoSecretKey = inputKey("Enter Telco Secret Key: ");

            //combine two key and convert to byte array
            byte[] fullKey = Util.xorWithKey(DatatypeConverter.parseHexBinary(itmxSecretKey), DatatypeConverter.parseHexBinary(telcoSecretKey));

            System.out.println("full key: "+ Util.byteArrayToHexString(fullKey));

            //encryption
            KeyManager manager = new KeyManager();
            byte[] encryptedKey = manager.encrypt(fullKey);
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
        String commandList = "genkey, otp";
        Options options = new Options();

        Option command = new Option("c", "command", true, "execution command, possible value : "+ commandList);
        command.setRequired(true);
        options.addOption(command);

        Option keyfile = new Option("f", "keyfile", true, "keyfile output in case of genkey, keyfile as input in case of otp");
        keyfile.setRequired(true);
        options.addOption(keyfile);

        Option interval = new Option("t", "request_date", true, "date to be test in format yyyyMMdd : 20180621");
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
                String intervalValue = cmd.getOptionValue("request_date");
                if (intervalValue == null) {
                    throw new InvalidFormatException("request_date is mandatory");
                }

                byte[] encryptedSeed = Files.readAllBytes(Paths.get(keyFilePath));
                byte[] seed = KeyManager.newInstance().decrypt(encryptedSeed);
                System.out.println("Verifying reference code....");
                generateRefCode(seed, intervalValue);

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
