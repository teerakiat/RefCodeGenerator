package th.co.itmx.util;

public class Util {

    public static void clearScreen() throws Exception {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
    }


    private static String byteArrayToHexString(byte[] byteArray){
        String hexString = "";

        for(int i = 0; i < byteArray.length; i++){
            String thisByte = "".format("%x", byteArray[i]);
            hexString += thisByte;
        }

        return hexString;
    }

}
