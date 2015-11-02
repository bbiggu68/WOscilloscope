package org.homenet.woscilloscope;

import android.util.Log;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public final class ReceiveBuffer {
    // Debugging
    private final static String TAG = "ReceiveBuffer";
    private static final boolean D = true;
    //
    public static final int MAXSIZE = 65536;
    public static byte[] mainBuf = new byte[MAXSIZE];
    public static int head = 0;
    public static int tail = 0;
    public static boolean bAcceptable = true;
    public static boolean bComplete = false;
    public static int cnt100msec = 0;

    public static void initBuffer() {
        head = 0;
        tail = 0;
        for (int i = 0; i < mainBuf.length; i++) {
            mainBuf[i] = 0;
        }
    }

    public static boolean addData(byte[] inData, int size) {
        if (D) Log.d(TAG, "head = " + head + ", size = " + size);
        if ((size + head) > MAXSIZE) {
            bAcceptable = false;
            head = head % MAXSIZE;
            return false;
        }
        System.arraycopy(inData, 0, mainBuf, head, size);
        head = head + size;
        //head = head % MAXSIZE;
        return true;
    }
}
