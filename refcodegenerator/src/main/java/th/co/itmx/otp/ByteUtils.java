package th.co.itmx.otp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by teerakiat on 8/14/2017 AD.
 */
public class ByteUtils {
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, x);
        return buffer.array();
    }
//
//    public static long bytesToLong(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
//        if(bytes.length < 8){
//            buffer.put(new byte[] {0}, 0, 1);
//        }
//        buffer.put(bytes, 0, bytes.length);
//        buffer.flip();//need flip
//        return buffer.getLong();
//    }

    public static int bytesToInt(byte[] bytes){
//        ByteBuffer i_buffer = ByteBuffer.allocate(Integer.BYTES);
//        i_buffer.put(bytes, 0, bytes.length);
//        i_buffer.flip();//need flip
//        return i_buffer.getInt();
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }
}
