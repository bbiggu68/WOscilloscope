package org.homenet.woscilloscope;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public class SocketManager implements Runnable {
    // Debugging
    private final static String TAG = "SocketManager";
    private static final boolean D = true;
    //
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
                        mReceiveThread = new Thread(mReciver, "DataReceiver");
                        mReceiveThread.start();
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
//            byte[] rcvBuffer = new byte[2048];
            byte[] rcvBuffer = new byte[20];
            byte[] rcvData = null;

            try {
                while (!mKillThread) {
                    mReceivePacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
                    try {
                        mSocket.setSoTimeout(5000);
                        mSocket.receive(mReceivePacket);

                        ReceiveBuffer.cnt100msec=0;

                        rcvData = new byte[mReceivePacket.getLength()];
                        System.arraycopy(mReceivePacket.getData(), 0, rcvData, 0, mReceivePacket.getLength());
                        if (D) Log.d(TAG, "Successful Received: Data is '" + rcvData.toString() + "' Size is '" + mReceivePacket.getLength());
                        if (mReceivePacket.getLength() > 0) {
                            if (ReceiveBuffer.addData(rcvData, mReceivePacket.getLength())) {
                                if (ReceiveBuffer.head != ReceiveBuffer.tail) {
                                    if (CommandBuilder.validateCommand()) {
                                        CommandBuilder.bUsableCommand = true;
                                        if (D) Log.d(TAG, "bUsableCommand = true");
                                    }
                                }
                            }

                        }


                    } catch (InterruptedIOException e) {
                        if (D) Log.d(TAG, "ReceiveTimeout!");
                    }
                    mReceivePacket = null;
                    rcvData = null;
                    rcvBuffer = null;
                    rcvBuffer = new byte[2048];
                }
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
    SocketManager(String hostip, int hostport) {
        this.mRemoteHostIP = hostip;
        this.mRemoteHostPort = hostport;
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

    //===================================================
    // public method : SocketManager.sendCommand()
    //===================================================
    public void sendCommand(byte cmd, byte arg) {
        Message msg = new Message();
        msg.what = ThreadMessage.SM_Send;
        msg.obj = CommandBuilder.makeSendCommand(cmd, arg);
        mHandler.sendMessage(msg);
    }

    public void sendCommand(byte cmd, int size, byte[] arg) {
        Message msg = new Message();
        msg.what = ThreadMessage.SM_Send;
        msg.obj = CommandBuilder.makeSendCommand(cmd,size,arg);
        mHandler.sendMessage(msg);
    }

    public void sendCommand(byte cmd) {
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
        }
        catch(Exception send_e)
        {
            if (D) Log.d(TAG, "Oops! Error Occurred in sending...");
        }
    }
}
