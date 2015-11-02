package org.homenet.woscilloscope;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by bbiggu on 2015. 11. 2..
 */
public class SocketManager implements Runnable {
    private String mRemoteHostIP = null;
    private int mRemoteHostPort = 5000;
    private int mLocalHostPort = 5000;
    private InetAddress mRemoteHostInetAddr = null;
    private DatagramSocket mSocket = null;
    private DatagramPacket mSendPacket = null;
    private DatagramPacket mReceivePacket = null;
    private boolean mKillThread = false;
    private Handler mHandler;

    public void run() {
        Looper.prepare();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ThreadMessage.SM_Connect:	// connect
                        connectHost(mRemoteHostIP, mRemoteHostPort); // Connect Remote Host
//                        mReceiveThread = new Thread(mReciver, "DataReceiver");
//                        mReceiveThread.start();
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
            Looper.myLooper().quit();
        }
    }

    //===================================================
    // SocketManager 객체의 초기화.
    //===================================================
    SocketManager() {
//        mReciver = new DataReceiver();
    }

    //===================================================
    // SocketManager 객체의 종료.
    //===================================================
    public void quit() {
//        mKillThread = true;
        // receivethread가 살아있다면 receivethread 종료 작업부터 하고 (mKillThread = true; receivethread.interrupted(), receivethread.join()

        mHandler.post(new QuitLooper());
    }

    public void connect() {
        mHandler.sendEmptyMessage(ThreadMessage.SM_Connect);
    }

    public void disconnect() {
        mHandler.sendEmptyMessage(ThreadMessage.SM_Disconnect);
    }

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

    private void connectHost(String hostip, int hostport) {
        setServerInetAddress();
        try {
            mSocket = new DatagramSocket(mLocalHostPort);
            //mSocket = new DatagramSocket();
            mSocket.connect(mRemoteHostInetAddr, mRemoteHostPort);
            Log.d("lds1000:WorkerThread", "Remote Host Connecting OK!");
            mKillThread = false;
        } catch (Exception e) {
            Log.d("lds1000:WorkerThread", "Oops! Error Occurred in connecting...");
        }
    }

    private void disconnectHost() {
        mKillThread = true;
        // receivethread.interrupte();
        // receivethread.join();
    }

    private void setServerInetAddress() {
        try {
            mRemoteHostInetAddr = InetAddress.getByName(mRemoteHostIP);
            Log.d("lds1000:WorkerThread", "Make Server Address OK!");
        } catch (Exception e) {
            Log.d("lds1000:WorkerThread", "Make Server Address Failure!");
        }
    }

    private void sendData(byte[] data) {
        //byte[] byteInData = data.getBytes();
        byte[] byteInData = data;
        mSendPacket = new DatagramPacket(byteInData, byteInData.length, mRemoteHostInetAddr, mRemoteHostPort);
        try
        {
            mSocket.send(mSendPacket);
            Log.d("lds1000:WorkerThread", "Successful Sent. Data is: '" + new String(byteInData) + "'");
        }
        catch(Exception send_e)
        {
            Log.d("lds1000:WorkerThread", "Oops! Error Occurred in sending...");
        }
    }
}
