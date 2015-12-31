package org.homenet.woscilloscope;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public final class CommandBuilder {
    // Debugging
    private final static String TAG = CommandBuilder.class.getSimpleName();
    private static final boolean D = true;
    //
    public static final int CMD_STX = 0;
    public static final int CMD_CMD = 1;
    public static final int CMD_SIZE1 = 2;
    public static final int CMD_SIZE2 = 3;
    public static final int CMD_DATA = 4;
    public static final int CMD_CS = 5;

    public static final int MAXSIZE = 65536;
    private final static int FRAME_SIZE = 1000;

//    public static int validState = CMD_STX;
    public static int validState = CMD_CMD;
    public static int validChkSum = 0;
    public static int validDataCnt = 0;
    public static int validDataSize = 0;
//    public static byte[] receiveCmd = new byte[FRAME_SIZE];
    public static byte[] receiveCmd = null;
    public static Queue<byte[]> rcvCmdQueue = new LinkedList<byte[]>();

    public static boolean bUsableCommand = false;

    public static boolean validateCommandAtOnce() {
        boolean ret = false;
        int[] retByte = {0};
        byte[] size = new byte[2];
        boolean loopexit = false;

        validState = CMD_CMD;

        while (!loopexit) {
            // 원형버퍼에 데이터가 없거나 버퍼 MaxSize가 초과되었다면 invalidate 이므로 리젝트
            if (ReceiveBuffer.tail == ReceiveBuffer.head) {
                ReceiveBuffer.tail = 0;
                ReceiveBuffer.head = 0;
                break;
            }
            retByte[0] = ReceiveBuffer.mainBuf[ReceiveBuffer.tail++];
            ReceiveBuffer.tail = ReceiveBuffer.tail % ReceiveBuffer.MAXSIZE;
            if(retByte[0] < 0) retByte[0] = 256 + retByte[0];
            switch (validState) {
                case CMD_CMD:
                    if (retByte[0] == WirelessProbeCMD.cmdReceivedData) {
                        validState = CMD_SIZE1;
                    } else {
                        if (D) Log.d(TAG, "Bad Received Data!!!");
                        validState = CMD_STX;
                        ReceiveBuffer.initBuffer((byte)0x00);
                        ret = false;
                    }
                    break;
                case CMD_SIZE1:
                    validState = CMD_SIZE2;
                    size[0] = (byte) retByte[0];
                    break;
                case CMD_SIZE2:
                    size[1] = (byte) retByte[0];
                    validDataSize = Utils.byteToShort(size);
                    validDataCnt = 0; // 헤더에 기록된 Size와 실제 수신된 데이터의 크기를 비교하기 위해
                    validState = CMD_DATA;
                    break;
                case CMD_DATA:
                    validDataCnt++;
                    if (validDataCnt == validDataSize) {

                        receiveCmd = new byte[FRAME_SIZE];
                        System.arraycopy(ReceiveBuffer.mainBuf, 3, receiveCmd, 0, validDataCnt);
                        ret = true;
                        loopexit = true;
                        ReceiveBuffer.initBuffer((byte)0x00);
                    } else {
//                        if (D) Log.d(TAG, "validDataCnt = " + validDataCnt +", validDataSize = " + validDataSize);
                    }
                    break;
            }
        }
        return ret;
    }

//    public static boolean validateCommandAtOnce() {
//        boolean ret = false;
//        int[] retByte = {0};
//        byte[] size = new byte[2];
//
//        while (true) {
//            // 원형버퍼에 데이터가 없거나 버퍼 MaxSize가 초과되었다면 invalidate 이므로 리젝트
//            if (ReceiveBuffer.tail == ReceiveBuffer.head) {
//                ReceiveBuffer.tail = 0;
//                ReceiveBuffer.head = 0;
//                break;
//            }
//
//            retByte[0] = ReceiveBuffer.mainBuf[ReceiveBuffer.tail++];
//            ReceiveBuffer.tail = ReceiveBuffer.tail % ReceiveBuffer.MAXSIZE;
//            if(retByte[0] < 0) retByte[0] = 256 + retByte[0];
//            switch (validState) {
////                case CMD_STX:
////                    if (retByte[0] == 0x02) validState = CMD_CMD;
////                    validChkSum = 0;
////                    break;
//                case CMD_CMD:
//                    validState = CMD_SIZE1;
////                    validChkSum = validChkSum + retByte[0];
////                    validChkSum = validChkSum % 256;
//                    break;
//                case CMD_SIZE1:
//                    validState = CMD_SIZE2;
////                    validDataSize = retByte[0] * 256;
//                    size[0] = (byte) retByte[0];
////                    validChkSum = validChkSum + retByte[0];
////                    validChkSum = validChkSum % 256;
//                    break;
//                case CMD_SIZE2:
////                    validDataSize = validDataSize + retByte[0];
//                    size[1] = (byte) retByte[0];
//                    validDataSize = Utils.byteToShort(size);
//                    validDataCnt = 0; // 헤더에 기록된 Size와 실제 수신된 데이터의 크기를 비교하기 위해
////                    if (validDataSize == 0) {
////                        validState = CMD_CS;
////                    } else {
//                        validState = CMD_DATA;
////                    }
////                    validChkSum = validChkSum + retByte[0];
////                    validChkSum = validChkSum % 256;
//                    break;
//                case CMD_DATA:
//                    validDataCnt++;
////                    validChkSum = validChkSum + retByte[0];
////                    validChkSum = validChkSum % 256;
////                    if (validDataCnt >= validDataSize) validState = CMD_CS;
//                    if (validDataCnt == validDataSize) {
//                        validState = CMD_CS;
//                    } else {
//                        if (D) Log.d(TAG, "validDataCnt = " + validDataCnt +", validDataSize = " + validDataSize);
//                    }
//                    break;
//                case CMD_CS:
////                    validState = CMD_STX;
//                    validState = CMD_CMD;
////                    if (retByte[0] == (255 - validChkSum)) {
//                        receiveCmd = new byte[FRAME_SIZE];
////                        System.arraycopy(ReceiveBuffer.mainBuf, 4, receiveCmd, 0, validDataCnt);
//                        System.arraycopy(ReceiveBuffer.mainBuf, 3, receiveCmd, 0, validDataCnt);
//                        ret = true;
////                    } else {
////                        if (D) Log.d(TAG, "Checksum Error Occurred!!!");
////                        validState = CMD_STX;
////                        ReceiveBuffer.initBuffer((byte)0x00);
////                        ret = false;
////                    }
//                    break;
//            }
//        }
//
//        return ret;
//    }

    public static boolean validateCommand() {
        boolean ret = false;
        int[] aData = {0};

        while (true) {
            if (!getByteFromMainBuffer(aData)) break;
            switch (validState) {
                case CMD_STX:
                    if (aData[0] == 0x02) validState = CMD_CMD;
                    receiveCmd[0] = (byte)aData[0];
                    validChkSum = 0;
                    break;
                case CMD_CMD:
                    validState = CMD_SIZE1;
                    receiveCmd[1] = (byte)aData[0];
                    validChkSum = validChkSum + aData[0];
                    validChkSum = validChkSum % 256;
                    break;
                case CMD_SIZE1:
                    validState = CMD_SIZE2;
                    receiveCmd[2] = (byte)aData[0];
                    validDataSize = aData[0] * 256;
                    validChkSum = validChkSum + aData[0];
                    validChkSum = validChkSum % 256;
                    break;
                case CMD_SIZE2:
                    receiveCmd[3] = (byte)aData[0];
                    validDataSize = validDataSize +aData[0];
                    validDataCnt = 0;
                    if (validDataSize == 0) {
                        validState = CMD_CS;
                    } else {
                        validState = CMD_DATA;
                    }
                    validChkSum = validChkSum + aData[0];
                    validChkSum = validChkSum % 256;
                    break;
                case CMD_DATA:
                    receiveCmd[4 + validDataCnt] = (byte)aData[0];
                    validDataCnt++;
                    validChkSum = validChkSum + aData[0];
                    validChkSum = validChkSum % 256;
                    if (validDataCnt >= validDataSize) validState = CMD_CS;
                    break;
                case CMD_CS:
                    validState = CMD_STX;
                    if (aData[0] ==(255 - validChkSum)) {
                        receiveCmd[5 + validDataCnt] = (byte)aData[0];
                        ret = true;
                    } else {
                        if (D) Log.d(TAG, "Checksum Error Occurred!!!");
                        validState = CMD_STX;
                        ReceiveBuffer.initBuffer((byte)0x00);
                        ret = false;
                    }
                    break;
            }
        }

        return ret;
    }
    public static boolean getByteFromMainBuffer(int[] retByte) {
        boolean ret = false;
        if (ReceiveBuffer.tail == ReceiveBuffer.head) {
            retByte[0] = 0;
            ReceiveBuffer.tail = 0;
            ReceiveBuffer.head = 0;
        } else {
            retByte[0] =ReceiveBuffer.mainBuf[ReceiveBuffer.tail++];
            ReceiveBuffer.tail = ReceiveBuffer.tail % ReceiveBuffer.MAXSIZE;
            if(retByte[0]<0) retByte[0]=256+ retByte[0];
            ret = true;
        }
        return ret;
    }
    public static byte[] makeSendCommand(byte inCMD, byte inVal) {
        byte[] OutData = new byte[8];
//        int chksum = 0;
        int add = 0;
//        OutData[add++] = 0x02;
        OutData[add++] = inCMD;
//        OutData[add++] = 0x00;	//Size1
//        OutData[add++] = 0x01;	//Size2
        OutData[add++] = inVal;
//        for (int i = 1; i < add; i++) {
//            chksum += OutData[i];
//            chksum %= 256;
//        }
//        OutData[add++] = (byte)(255 - chksum);
        for (int i = 2; i < 8; i++) {
            OutData[add++] = 0x00;
        }

        byte[] retData = new byte[add];
        System.arraycopy(OutData, 0, retData, 0, add);
        return retData;
    }

    public static byte[] makeSendCommand(byte inCMD) {
        byte[] OutData = new byte[1024];
        int chksum = 0;
        int add = 0;
        OutData[add++] = 0x02;
        OutData[add++] = inCMD;
        OutData[add++] = 0;	//size1
        OutData[add++] = 0;	//size2
        for (int i = 1; i < add; i++) {
            chksum += OutData[i];
            chksum %= 256;
        }
        OutData[add++] = (byte)(255 - chksum);

        byte[] retData = new byte[add];
        System.arraycopy(OutData, 0, retData, 0, add);
        return retData;
    }

    public static byte[] makeSendCommand(byte inCMD, int size, byte[] inBuf) {
        byte[] OutData = new byte[2048];
        int chksum = 0;
        int add = 0;
        OutData[add++] = 0x02;
        OutData[add++] = inCMD;
        OutData[add++] = (byte)(size / 256);	//size1
        OutData[add++] = (byte)(size);	//size2
        for (int i = 0; i < size; i++) OutData[add++] = inBuf[i];
        for (int i = 1; i < add; i++) {
            chksum += OutData[i];
            chksum %= 256;
        }
        OutData[add++] = (byte)(255 - chksum);

        byte[] retData = new byte[add];
        System.arraycopy(OutData, 0, retData, 0, add);
        return retData;
    }

    public final class WirelessProbeCMD {
        //=== Command Only
        public static final byte cmdScanProbe = (byte) 0xF0;        // 프루브 스캐닝 패킷
        //=== Command with Arguments
        // Probe Info : Name, Type
        public static final byte cmdScanResponse = (byte) 0xF1;     // 프루브 스캐닝에 대한 응답으로 수신되는 패킷
        //=== Command with Arguments
        // Vertical Scale, Vertical Position, Horizontal Scale, Trigger Mode, Trigger Type, Trigger Level, Trigger Position, Selected Channel
        // Vertical Scale : Voltage Scale (Div별). Depend on Probe. (2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000 mV) => 1Byte 인덱스
        // Vertical Position : fine = 0.1% 씩 1000 단계, coarse = 1% 씩 100단계. (0~100%) 2Byte Integer
        // Horizontal Scale : Time Scale (Div별). Common. (0.01, 0.02, 0.04, 0.1, 0.2, 0.4, 1, 2, 4, 10, 20, 40, 100, 200, 400, 1000, 2000, 4000, 10000 ms) = 4Byte Float
        // Trigger Mode :  Depend on Probe. (Auto=0, Normal=1, Single=2) 4Bit
        // Trigger Type :  Depend on Probe. (Rising Edge=0, Falling Edge=1) 4Bit
        // Trigger Level : Depend on Probe. fine = 1~200 단계, coarse = 1~100 단계. 현재 Vertical Scale에 따라 값이 달라짐. ex) 2mV라면 Full 20mV fine 일 때는 -10mV ~ 10mV
        // Trigger Position : Depend on Probe. fine = 0.1% 씩 1000 단계, coarse = 1% 씩 100단계. (0~100%) 2Byte Integer
        public static final byte cmdSetParameter = (byte) 0xF2;
        public static final byte cmdQueryParameter = (byte) 0xF3;
        public static final byte cmdQueryBatteryStatus = (byte) 0xF4;

        public static final byte cmdSetOperation = (byte) 0x10;
        public static final byte subcmdStart = (byte) 0x01;
        public static final byte subcmdStop = (byte) 0x00;
        // Trigger Mode = Normal인 경우)무조건 100ms 주기로 1Frame씩(1000Byte)수신될 것임.  or Trigger Mode = Single인 경우 한번 수신하고 전송 중단.
        public static final byte cmdReceivedData = (byte) 0x11;

        //============================================================
        public static final byte cmdQueryConfigResult = (byte) 0xE2;
        // Battery Status : 배터리 잔량율 (0 ~ 100 %)
        public static final byte cmdQueryBatteryStatusResult = (byte) 0xE4;

        //=== Command with Arguments
        // 가변 크기로 100ms주기로 수신될 것임. 1Frame중에 몇 번째 인지를 표시하는 헤더가 있어야 하나?
        public static final byte cmdReceivedFragmantedData = (byte) 0xD1;
        public static final byte cmdStartStopQuery = (byte) 0x1A;
        public static final byte cmdStartStopQueryReturn = (byte) 0x1F;

        public static final byte cmdPulseResult = (byte) 0x20;
        public static final byte cmdPulseReqest = (byte) 0x21;

        public static final byte cmdSetTriggerMode = (byte) 0x30;
        public static final byte cmdSetVerticalScale = (byte) 0x30;
        public static final byte cmdSetHorizontalScale = (byte) 0x31;


        public static final byte cmdStatusAllQuery = (byte) 0xAA;
        public static final byte cmdStatusAllQueryReturn = (byte) 0xAF;

        public static final byte cmdPsConfigSet = (byte) 0x50;
        public static final byte cmdPsConfigSetQuery = (byte) 0x5A;
        public static final byte cmdPsConfigSetQueryReturn = (byte) 0x5F;

        public static final byte cmdTempConfigSet = (byte) 0x60;
        public static final byte cmdTempConfigSetQuery = (byte) 0x6A;
        public static final byte cmdTempConfigSetQueryReturn = (byte) 0x6F;

        public static final byte cmdReceiveOKReturn = (byte) 0xFF;
    }
}
