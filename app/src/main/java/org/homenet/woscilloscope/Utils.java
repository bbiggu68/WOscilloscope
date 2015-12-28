package org.homenet.woscilloscope;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public final class Utils {

    public static float arr2float (byte[] arr, int start) {

        int i = 0;
        int len = 4;
        int cnt = 0;
        byte[] tmp = new byte[len];

        for (i = start; i < (start + len); i++) {
            tmp[cnt] = arr[i];
            cnt++;
        }

        int accum = 0;
        i = 0;
        for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
            accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
            i++;
        }
        return Float.intBitsToFloat(accum);
    }

    public static byte[] float2bytes(float value) {

        byte[] array = new byte[4];

        int intBits=Float.floatToIntBits(value);

        array[0]=(byte)((intBits&0x000000ff)>>0);
        array[1]=(byte)((intBits&0x0000ff00)>>8);
        array[2]=(byte)((intBits&0x00ff0000)>>16);
        array[3]=(byte)((intBits&0xff000000)>>24);

        return array;
    }

    public static short byteToShort(byte[] data) {

        short result = 0;

        result += ((data[0] & 0xff));
        result += ((data[1] & 0xff) << (8));

        return result;
    }

    public static short readShort(byte[] data, int offset) {

        return (short) (((data[offset] & 0xff)) | ((data[offset + 1] & 0xff) << 8));
    }

    public static byte[] shortToByteArray(short value) {
        return new byte[] { (byte) ((value & 0xff00) >> 8), (byte) (value & 0x00ff) };
    }

    public static int readInt(byte[] data, int offset) {

        return (int) (((data[offset] & 0xff)) | ((data[offset + 1] & 0xff) << 8) | ((data[offset + 2] & 0xff) << 16) | ((data[offset + 3] & 0xff) << 24));
    }
}
