package th.co.itmx;

import org.apache.commons.cli.*;
import th.co.itmx.keystore.KeyManager;
import th.co.itmx.otp.OTPManager;
import th.co.itmx.util.InvalidFormatException;

import java.nio.file.Files;
import java.nio.file.Paths;


public class Main {

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
                KeyManager manager = KeyManager.newInstance();
                String outputFile = cmd.getOptionValue("keyfile");
                manager.generateKey(outputFile);
            } else if(commandValue.equalsIgnoreCase("otp")) {
                OTPManager manager = OTPManager.newInstance();
                String keyFilePath = cmd.getOptionValue("keyfile");
                String intervalValue=cmd.getOptionValue("time");
                if(intervalValue == null){
                    throw new InvalidFormatException("Expiry time is mandatory");
                }

                byte[] encryptedSeed = Files.readAllBytes(Paths.get(keyFilePath));
                byte[] seed = KeyManager.newInstance().decrypt(encryptedSeed);
                manager.GenerateRefCode(seed, Integer.parseInt(intervalValue));
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
}
