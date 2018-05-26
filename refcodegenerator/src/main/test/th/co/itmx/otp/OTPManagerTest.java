package th.co.itmx.otp;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sun.reflect.annotation.ExceptionProxy;
import th.co.itmx.otp.exception.InvalidFormatException;

public class OTPManagerTest {
    static OTPManager otpManager;
    static String nationalId;
    static String mobileNumber;
    static String requestDate;
    static byte[] seed;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @BeforeClass
    public static void init(){
        otpManager = OTPManager.newInstance();
        nationalId="2-0345-88765-54-3";
        mobileNumber="+66745532145";
        seed = new byte[]{'a','b','c','d','e','f','g'};
        requestDate = "20180621";
    }

    @Test
    public void generateRefCode_Success() throws Exception{
        String otp = otpManager.generateRefCode(seed, requestDate, nationalId, mobileNumber);
        System.out.println(otp);
    }

    @Test
    public void generateRefCode_WrongRequestDate() throws Exception{
        expectedEx.expect(InvalidFormatException.class);
        expectedEx.expectMessage("Invalid request_date format, must be in yyyyMMDD format ex. 20180621");
        otpManager.generateRefCode(seed, "abcdefg", nationalId, mobileNumber);
    }

    @Test
    public void generateRefCode_WrongNationalId() throws Exception{
        expectedEx.expect(InvalidFormatException.class);
        expectedEx.expectMessage("Invalid Id format, correct format must be in x-xxxx-xxxxx-xx-x");
        otpManager.generateRefCode(seed, requestDate, "teerakiat", mobileNumber);
    }

    @Test
    public void generateRefCode_WrongMobilePhoneNumber() throws Exception{
        expectedEx.expect(InvalidFormatException.class);
        expectedEx.expectMessage("Invalid mobile number exception, correct format must be +66857761709");
        otpManager.generateRefCode(seed, requestDate, nationalId, "0998765432");
    }
}