package th.co.itmx.otp;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static junit.framework.TestCase.assertEquals;

public class KeyManagerTest {

    static PrivateKey pk;
    static PublicKey pb;
    static KeyManager km;

    @BeforeClass
    public static void init() throws Exception{
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024); // You can set a different value here

        KeyPair kp = kpg.generateKeyPair();
        pk = kp.getPrivate();
        pb = kp.getPublic();

        km = KeyManager.newInstance();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    @Test
    public void encrypt_Success() throws Exception{
        String test = "hello";
        byte[] encrypted = km.encrypt(test.getBytes(), pb, 1);

        Date startDate = km.getStartDate(encrypted);
        Date endDate = km.getEndDate(encrypted);

        Date expectCurrentDate = new Date();

        assertEquals(DateUtils.truncate(startDate, Calendar.DATE),
                DateUtils.truncate(expectCurrentDate,Calendar.DATE));

        Date expectEndDate = DateUtils.addYears(expectCurrentDate,1);

        assertEquals(DateUtils.truncate(endDate, Calendar.DATE),
                DateUtils.truncate(expectEndDate, Calendar.DATE));

    }
}
