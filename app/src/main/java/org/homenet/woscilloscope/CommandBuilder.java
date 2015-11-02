package org.homenet.woscilloscope;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public final class CommandBuilder {
    // Debugging
    private final static String TAG = "CommandBuilder";
    private static final boolean D = true;
    //
    public static final int CMD_STX = 0;
    public static final int CMD_CMD = 1;
    public static final int CMD_SIZE1 = 2;
    public static final int CMD_SIZE2 = 3;
    public static final int CMD_DATA = 4;
    public static final int CMD_CS = 5;

    public static final int MAXSIZE = 65536;

    public static int validState = CMD_STX;
    public static int validChkSum = 0;
    public static int validDataCnt = 0;
    public static int validDataSize = 0;
    public static byte[] receiveCmd = new byte[MAXSIZE];

    public static boolean bUsableCommand = false;

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
                        validState = CMD_STX;
                        ReceiveBuffer.initBuffer();
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
        int chksum = 0;
        int add = 0;
        OutData[add++] = 0x02;
        OutData[add++] = inCMD;
        OutData[add++] = 0x00;	//Size1
        OutData[add++] = 0x01;	//Size2
        OutData[add++] = inVal;
        for (int i = 1; i < add; i++) {
            chksum += OutData[i];
            chksum %= 256;
        }
        OutData[add++] = (byte)(255 - chksum);

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

    public final class WiressProbeCMD {
        public static final byte cmdDeviceOKQuery = (byte) 0x01;

        public static final byte cmdStartStop = (byte) 0x10;
        public static final byte subcmdStart = (byte) 0x01;
        public static final byte subcmdStop = (byte) 0x00;

        public static final byte cmdStartStopQuery = (byte) 0x1A;
        public static final byte cmdStartStopQueryReturn = (byte) 0x1F;

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
