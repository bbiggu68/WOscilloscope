package org.homenet.woscilloscope;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public final class ThreadMessage {
    // MainActivity
    public static final int MA_DrawData = 1;
    // Timer4StatusAllQuery
    public static final int TM_TimerSendQuery = 1;
    // Timer4ReceivedCommands
    public static final int TM_TimerReceiveProcess = 2;


    // SocketManger
    public static final int SM_Connect = 1;
    public static final int SM_Disconnect = 2;
    public static final int SM_Send = 3;
}
