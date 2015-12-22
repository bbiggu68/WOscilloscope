package org.homenet.woscilloscope;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public class SocketManager implements Runnable {
    // Debugging
    private final static String TAG = SocketManager.class.getSimpleName();
    private static final boolean D = true;
    // private 상수
    private final static int FRAME_SIZE = 10000;
    private final static int RCV_BUFFER_SIZE = FRAME_SIZE + 4 + 1;
//    private final static int RCV_BUFFER_SIZE = 1000;
    private final static int SO_TIMEOUT_MS = 5000;
    //
    private MainActivity mCallerClone = null;
    private String mRemoteHostIP = null;
    private int mRemoteHostPort = 5000;
    private int mLocalHostPort = 5000;
    private InetAddress mRemoteHostInetAddr = null;
    private DatagramSocket mSocket = null;
    private DatagramPacket mSendPacket = null;
    private DatagramPacket mReceivePacket = null;
    private boolean mKillThread = false;
    private Handler mHandler;
    private DataReceiver mReciver = null;
    private Thread mReceiveThread = null;

    public void run() {
        Looper.prepare();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ThreadMessage.SM_Connect:	// connect
                        connectHost(mRemoteHostIP, mRemoteHostPort); // Connect Remote Host
                        break;
                    case ThreadMessage.SM_Disconnect:	// disconnect
                        disconnectHost();
                        break;
                    case ThreadMessage.SM_Send:	// send
                        sendData((byte[])msg.obj);
                        break;
                }
            }
        };
        Looper.loop();
    }

    // Inner Runnable which can be posted to the handler
    class QuitLooper implements Runnable {
        @Override
        public void run()
        {
            //
            Looper.myLooper().quit();
        }
    }

    class DataReceiver implements Runnable {
        public void run() {
            byte[] rcvBuffer = new byte[RCV_BUFFER_SIZE];

            try {
                while (!mKillThread) {
                    mReceivePacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
                    try {
                        mSocket.setSoTimeout(SO_TIMEOUT_MS);
                        mSocket.receive(mReceivePacket);

//                        if (D) Log.d(TAG, "Successful Received: Data is '" + rcvBuffer.toString() + "'\n Size is '" + mReceivePacket.getLength());
                        mCallerClone.parentApplication.starttime = System.nanoTime();
                        if (mReceivePacket.getLength() > 0) {
                            mRemoteHostInetAddr = mReceivePacket.getAddress();
                            mRemoteHostIP = mReceivePacket.getAddress().getHostAddress();
                            mRemoteHostPort = mReceivePacket.getPort();
                            if (ReceiveBuffer.addData(rcvBuffer, mReceivePacket.getLength())) {
                                if (ReceiveBuffer.head != ReceiveBuffer.tail) {
//                                    if (CommandBuilder.validateCommand()) {
                                    if (CommandBuilder.validateCommandAtOnce()) {
                                        CommandBuilder.rcvCmdQueue.offer(CommandBuilder.receiveCmd);
                                        // 수신된 데이터가 완성된 커맨드(프레임)이므로 처리(그래프 표시) 가능하다.
                                        // 그래프 그리는 스레드에게 메시지를 보내 처리하도록 한다.
                                        CommandBuilder.bUsableCommand = true;
//                                        if (D) Log.d(TAG, "bUsableCommand = true");
                                        mCallerClone.handlerMain.sendEmptyMessage(ThreadMessage.MA_PROC_RCV_CMD);
                                    }
                                }
                            }
                        }
                    } catch (InterruptedIOException e) {
                        if (D) Log.d(TAG, "ReceiveTimeout!");
                    }
                    mReceivePacket = null;
                    rcvBuffer = new byte[RCV_BUFFER_SIZE];
                }
            //} catch (PortUnreachableException e) {
            } catch (SocketException e) {
                // 연결 대상이 없을 때 (Probe Simulator가 실행되지 않은 상태일 때)
                if (D) Log.d(TAG, "PortUnreachableException");
            } catch (Exception e) {
                if (D) Log.d(TAG, "Error Occurred in receiving");
            } finally {
                mSocket.close();
                if (D) Log.d(TAG, "Socket Closed!");
            }
        }
    }

    //===================================================
    // SocketManager 객체의 초기화.
    // SocketManager Host IP, Host Port 설정.
    //===================================================
    SocketManager(String hostip, int hostport, MainActivity caller) {
        this.mRemoteHostIP = hostip;
        this.mRemoteHostPort = hostport;
        this.mCallerClone = caller;
        mReciver = new DataReceiver();
    }

    //===================================================
    // SocketManager 객체의 종료.
    //===================================================
    public void quit() {
        mKillThread = true;
        mReceiveThread.interrupt();
        try { mReceiveThread.join(); } catch (InterruptedException e) {;}
        mHandler.post(new QuitLooper());
    }

    //===================================================
    // public method : SocketManager.connect()
    //===================================================
    public void connect() {
        mHandler.sendEmptyMessage(ThreadMessage.SM_Connect);
    }

    //===================================================
    // public method : SocketManager.disconnect()
    //===================================================
    public void disconnect() {
        mHandler.sendEmptyMessage(ThreadMessage.SM_Disconnect);
    }

    public void startProbe() {
        mReceiveThread = new Thread(mReciver, "DataReceiver");
        sendCommand(CommandBuilder.WirelessProbeCMD.cmdStartStop, CommandBuilder.WirelessProbeCMD.subcmdStart);
        mSocket.close();
        try {
            mSocket = new DatagramSocket(mLocalHostPort);
            //mSocket = new DatagramSocket();
            mSocket.setBroadcast(false);
//            mSocket.connect(mRemoteHostInetAddr, mRemoteHostPort);
            if (D) Log.d(TAG, "Remote Host Connecting OK!");
            mKillThread = false;
        } catch (Exception e) {
            if (D) Log.d(TAG, "Oops! Error Occurred in connecting...");
        }
        mReceiveThread.start();
    }
    public void stopProbe() {
        sendCommand(CommandBuilder.WirelessProbeCMD.cmdStartStop, CommandBuilder.WirelessProbeCMD.subcmdStop);
    }
    //===================================================
    // public method : SocketManager.sendCommand()
    //===================================================
    public void sendCommand(byte cmd, byte arg) {
//        Message msg = Message.obtain(mHandler, ThreadMessage.SM_Send, CommandBuilder.makeSendCommand(cmd, arg));
//        mHandler.sendMessage(msg);

        Message msg = new Message();
        msg.what = ThreadMessage.SM_Send;
        msg.obj = CommandBuilder.makeSendCommand(cmd,arg);
        mHandler.sendMessage(msg);
    }

    public void sendCommand(byte cmd, int size, byte[] arg) {
//        Message msg = Message.obtain(mHandler, ThreadMessage.SM_Send, CommandBuilder.makeSendCommand(cmd,size,arg));
//        mHandler.sendMessage(msg);

        Message msg = new Message();
        msg.what = ThreadMessage.SM_Send;
        msg.obj = CommandBuilder.makeSendCommand(cmd,size,arg);
        mHandler.sendMessage(msg);
    }

    public void sendCommand(byte cmd) {
//        Message msg = Message.obtain(mHandler, ThreadMessage.SM_Send, CommandBuilder.makeSendCommand(cmd));
//        mHandler.sendMessage(msg);

        Message msg = new Message();
        msg.what = ThreadMessage.SM_Send;
        msg.obj = CommandBuilder.makeSendCommand(cmd);
        mHandler.sendMessage(msg);
    }

    //===================================================
    // private methods
    //===================================================
    private void connectHost(String hostip, int hostport) {
        setServerInetAddress();
        try {
            mSocket = new DatagramSocket(mLocalHostPort);
            //mSocket = new DatagramSocket();
            mSocket.setBroadcast(true);
            mSocket.connect(mRemoteHostInetAddr, mRemoteHostPort);
            if (D) Log.d(TAG, "Remote Host Connecting OK!");
            mKillThread = false;
        } catch (Exception e) {
            if (D) Log.d(TAG, "Oops! Error Occurred in connecting...");
        }
    }

    private void disconnectHost() {
        mKillThread = true;
        mReceiveThread.interrupt();
        try { mReceiveThread.join(); } catch (InterruptedException e) {;}
        mSocket.close();
    }

    private void setServerInetAddress() {
        try {
            mRemoteHostInetAddr = InetAddress.getByName(mRemoteHostIP);
            if (D) Log.d(TAG, "Make Server Address OK!");
        } catch (Exception e) {
            if (D) Log.d(TAG, "Make Server Address Failure!");
        }
    }

    private void sendData(byte[] data) {
        //byte[] byteInData = data.getBytes();
//        byte[] byteInData = data;
        mSendPacket = new DatagramPacket(data, data.length, mRemoteHostInetAddr, mRemoteHostPort);
        try
        {
            mSocket.send(mSendPacket);
            if (D) Log.d(TAG, "Successful Sent. Data is: '" + new String(data) + "'");
            mSocket.setBroadcast(false);
        }
        catch(Exception send_e)
        {
            if (D) Log.d(TAG, "Oops! Error Occurred in sending...");
        }

    }
}
