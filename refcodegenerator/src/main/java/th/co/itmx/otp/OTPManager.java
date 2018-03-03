package th.co.itmx.otp;

import th.co.itmx.otp.exception.InvalidFormatException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OTPManager {

    public static final int TOKEN_LENGTH = 5;

    public static OTPManager newInstance() {
        return new OTPManager();
    }

    private String HOTP(String data, byte[] seed) throws Exception {
//        System.out.println(data);

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(seed, "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hmacArr = sha256_HMAC.doFinal(data.getBytes());
        int offsetBits = hmacArr[hmacArr.length - 1] & 0b00001111;

//        System.out.println(offsetBits);

        //copy 4 bytes from offsetbits
        final int BYTE_LENGTH = 4;
        byte[] tokenBytes = new byte[BYTE_LENGTH];
        System.arraycopy(hmacArr, offsetBits, tokenBytes, 0, tokenBytes.length - 1);

        tokenBytes[0] &= 0b01111111;
        int tokenNumber = ByteUtils.bytesToInt(tokenBytes);
        String token = Objects.toString(tokenNumber, null);
        return token.substring(token.length() - TOKEN_LENGTH);
    }


    private long[] getEpoch(int interval) {
        long epoch_raw = Instant.now().toEpochMilli();

        double current_epoch_raw = (double) epoch_raw / interval;
        double decimal_point = current_epoch_raw % 1;

//        System.out.println(decimal_point);

        //calculate time
        long current_epoch = (long) Math.floor(current_epoch_raw);
        long next_epoch = 0;
        if (decimal_point <= 0.2) {
            next_epoch = (long) Math.floor((epoch_raw - interval) / interval);
        } else if (decimal_point >= 0.8) {
            next_epoch = (long) Math.floor((epoch_raw + interval) / interval);
        }
        return new long[]{current_epoch, next_epoch};
    }

    public String generateRefCode(byte[] seed, int interval, String nationalId, String mobile) throws Exception {
        if (interval <= 0) {
            throw new InvalidFormatException("Expiry time must greater than 0");
        }

        long epoch_raw = Instant.now().toEpochMilli();

        long current_epoch = (long) Math.floor(epoch_raw / interval);

        return HOTP(nationalId + mobile + Long.toString(current_epoch), seed);
    }

    public void getPossibleRefCode(byte[] seed, int interval, String nationId, String mobile) throws Exception {

        if (interval <= 0) {
            throw new InvalidFormatException("Expiry time must greater than 0");
        }

        long[] epoch = getEpoch(interval * 1000);
//        for (long anEpoch : epoch) {
//            System.out.println(anEpoch);
//        }

        List<String> otp = new ArrayList<>();
        otp.add(HOTP(nationId + mobile + Long.toString(epoch[0]), seed));

        if (epoch[1] != 0) {
            otp.add(HOTP(nationId + mobile + Long.toString(epoch[1]), seed));
        }

        otp.forEach(System.out::println);
    }
}
