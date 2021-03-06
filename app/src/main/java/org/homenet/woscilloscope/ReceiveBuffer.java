package org.homenet.woscilloscope;

import android.util.Log;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public final class ReceiveBuffer {
    // Debugging
    private final static String TAG = ReceiveBuffer.class.getSimpleName();
    private static final boolean D = true;
    //
    public static final int MAXSIZE = 65536;
    public static byte[] mainBuf = new byte[MAXSIZE];
    public static int head = 0;
    public static int tail = 0;
    public static boolean bAcceptable = true;
    public static boolean bComplete = false;

    public static void initBuffer(byte fillvalue) {
        head = 0;
        tail = 0;
        for (int i = 0; i < mainBuf.length; i++) {
            mainBuf[i] = fillvalue;
        }
    }

    public static boolean addData(byte[] inData, int size) {
//        if (D) Log.d(TAG, "head = " + head + ", size = " + size);
        if ((size + head) > MAXSIZE) {
            // 원형 버퍼가 차고 넘쳐서 더 이상 데이터를 담아놓을 수 없는 경우, 전달 받은 데이터는 반려.
            // UDP DatagramPacket을 사용한다면 이렇게 나눠져서 들어오는 일은 없다고 봐도 된다. (가정)
            bAcceptable = false;
            head = head % MAXSIZE;
            return false;
        }
        // 원형 버퍼에 데이터를 복사해 넣고 헤더를 복사해 넣은 크기만큼 이동(증가) 시킨다.
        System.arraycopy(inData, 0, mainBuf, head, size);
        head = head + size;
        //head = head % MAXSIZE;
        return true;
    }
}
