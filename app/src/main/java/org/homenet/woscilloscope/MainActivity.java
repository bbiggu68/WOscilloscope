package org.homenet.woscilloscope;

import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    // Debugging
    private final static String TAG = "MainActivity";
    private static final boolean D = true;
    //
    public AppEntryPoint parentApplication = null;
    private SocketManager mSocketManagingClass = null;
    private Thread mThread4Socket = null;
    public InnerHandler handlerMain;

    static class InnerHandler extends Handler {
        WeakReference<MainActivity> mMainAct;

        InnerHandler(MainActivity mMainActRef) {
            mMainAct = new WeakReference<MainActivity>(mMainActRef);
        }

        public void handleMessage(Message msg) {
            MainActivity theMain = mMainAct.get();
            switch (msg.what) {
                case ThreadMessage.MA_PROC_RCV_CMD:
                    theMain.processReceiveCommand();
                    break;
            }
        }
    }
    //==========================================
    // Activity Callback
    //==========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handlerMain = new InnerHandler(MainActivity.this);
        parentApplication = (AppEntryPoint)getApplication();
    }

    @Override
    protected void onStart () {
        super.onStart();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop () {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // 매니페스트 파일에 activity 태그에 android:configChanges="orientation|screenSize|keyboardHidden"
        // 설정이 되어 있는 경우에만 호출된다. 이렇게 configChanges 설정을 하면 화면방향 변경시 액티비티 재생성 하지 않음.
        super.onConfigurationChanged(newConfig);
    }
    //==========================================
    // User Methods
    //==========================================
    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                // Start SocketManager Thread
                mSocketManagingClass = new SocketManager("10.0.1.21", 5000, this);
                mThread4Socket = new Thread(mSocketManagingClass, "SocketMgr");
                mThread4Socket.setDaemon(true); // UI 스레드가 가면 같이 간다.
                mThread4Socket.start();
                // 스레드 start()하고 조금 기다려야 스레드의 run()이 실행되서 핸들러가 만들어진다.
                try { Thread.sleep(50); } catch (InterruptedException e) {;}
                mSocketManagingClass.connect();
                break;
            case R.id.btnDisconnect:
                mSocketManagingClass.disconnect();
                if (mThread4Socket.isAlive()) {
                    mSocketManagingClass.quit();
                }
                break;
            case R.id.btnStart:
                Button tmpButton = (Button)v;

                if (tmpButton.getText().equals(getResources().getText(R.string.btnstart_caption))) {
                    tmpButton.setText(getResources().getText(R.string.btnstop_caption));
                    mSocketManagingClass.sendCommand(CommandBuilder.WirelessProbeCMD.cmdStartStop, CommandBuilder.WirelessProbeCMD.subcmdStart);
                } else {
                    tmpButton.setText(getResources().getText(R.string.btnstart_caption));
                    mSocketManagingClass.sendCommand(CommandBuilder.WirelessProbeCMD.cmdStartStop, CommandBuilder.WirelessProbeCMD.subcmdStop);
                }

                break;
        }
    }
    private void processReceiveCommand() {
        int add = 0;
        byte[] inB = new byte[4];
        String temp="";
        switch (CommandBuilder.receiveCmd[1])
        {
            case CommandBuilder.WirelessProbeCMD.cmdReceiveOKReturn:
//                mCfgClass.ReceiveOK = true;
//                synchronized (mCfgClass) {
//                    mCfgClass.notify();
//                }
                break;
            case CommandBuilder.WirelessProbeCMD.cmdStartStopQueryReturn:
//                mCfgClass.START = 1;
//                mCfgClass.ReceiveOK = true;
//                synchronized (mCfgClass) {
//                    mCfgClass.notify();
//                }
                break;
            case CommandBuilder.WirelessProbeCMD.cmdStatusAllQueryReturn:
//                add = 4;
//                for (int i = 0; i < mCfgClass.HW_No; i++)
//                {
//                    mCfgClass.HW_I_Status[i] =  CommandBuilder.receiveCmd[add++];
//                    for(int j =0; j< 4;j++) inB[j] = CommandBuilder.receiveCmd[add++];
//                    mCfgClass.HW_I_Value[i] = Utils.arr2float(inB, 0);
//                }
//
//                for (int i = 0; i < mCfgClass.TempNo; i++)
//                {
//                    for (int j = 0; j < 4; j++) inB[j] = CommandBuilder.receiveCmd[add++];
//                    mCfgClass.Temp_Value[i] = Utils.arr2float(inB, 0);
//                }
//
//                setWarningProtectionInfo();
//                // Draw Cell Data
//                mMessageHandler.sendEmptyMessage(THREADMSG.MA_DrawData);
                break;
            case CommandBuilder.WirelessProbeCMD.cmdPsConfigSetQueryReturn:
//                add=4;
//                int no =  (int)CommandBuilder.receiveCmd[add++];
//                byte[] cName = new byte[no];
//
//                for(int i=0;i<no;i++) cName[i] = CommandBuilder.receiveCmd[add++];
//
//                temp = new String(cName);
//                mCfgClass.FileName =temp;// cName.toString();
//                mCfgClass.PsNo= (int)CommandBuilder.receiveCmd[add++];
//
//                for(int i=0;i<4;i++) inB[i] = CommandBuilder.receiveCmd[add++];
//                mCfgClass.VsetMaster = Utils.arr2float(inB, 0);
//
//                for(int i=0;i<4;i++) inB[i] = CommandBuilder.receiveCmd[add++];
//                mCfgClass.IsetMaster = Utils.arr2float(inB, 0);
//
//                for(int i=0;i<4;i++) inB[i] = CommandBuilder.receiveCmd[add++];
//                mCfgClass.IProtect_Master = Utils.arr2float(inB, 0);
//
//                for(int i=0;i<mCfgClass.PsNo;i++)
//                {
//                    mCfgClass.PsEn[i] =  (int)CommandBuilder.receiveCmd[add++];
//                    mCfgClass.StringNo[i] =  (int)CommandBuilder.receiveCmd[add++];
//
//                    for(int j=0;j<4;j++) inB[j] = CommandBuilder.receiveCmd[add++];
//                    mCfgClass.Vset[i] = Utils.arr2float(inB, 0);
//
//                    for(int j=0;j<4;j++) inB[j] = CommandBuilder.receiveCmd[add++];
//                    mCfgClass.Iset[i] = Utils.arr2float(inB, 0);
//
//                    for(int j=0;j<4;j++) inB[j] = CommandBuilder.receiveCmd[add++];
//                    mCfgClass.IProtect[i] = Utils.arr2float(inB, 0);
//
//                    no = (int)CommandBuilder.receiveCmd[add++];
//                    cName = new byte[no];
//                    for(int j=0;j<no;j++)  cName[j] = CommandBuilder.receiveCmd[add++];
//                    temp = new String(cName);
//                    mCfgClass.PsName[i] =temp;// cName.toString();
//                }
//                mCfgClass.ReceiveOK = true;
                break;
            case CommandBuilder.WirelessProbeCMD.cmdTempConfigSetQueryReturn:
//                add=4;
//                mCfgClass.TempNo  =  (int)CommandBuilder.receiveCmd[add++];
//
//                for(int i=0;i<4;i++) inB[i] = CommandBuilder.receiveCmd[add++];
//                mCfgClass.TempProtectMaster = Utils.arr2float(inB, 0);
//
//                for(int i=0;i<mCfgClass.TempNo;i++)
//                {
//                    mCfgClass.TempEn[i] =  (int)CommandBuilder.receiveCmd[add++];
//
//                    for(int j=0;j<4;j++) inB[j] = CommandBuilder.receiveCmd[add++];
//                    mCfgClass.TempProtect[i] = Utils.arr2float(inB, 0);
//
//                    no = (int)CommandBuilder.receiveCmd[add++];
//                    cName = new byte[no];
//                    for(int j=0;j<no;j++)  cName[j] = CommandBuilder.receiveCmd[add++];
//                    temp = new String(cName);
//                    mCfgClass.TempName[i] = temp;
//                }
//                mCfgClass.ReceiveOK = true;
                break;
            case CommandBuilder.WirelessProbeCMD.cmdPulseResult:
                if (D) Log.d(TAG, "Receive Pulse Success!!!");
                parentApplication.endtime = System.nanoTime();
                if (D) Log.d(TAG, "Time of Data Receiving = " + ((parentApplication.endtime - parentApplication.starttime) / 1000000) + "ms");
//                add=4;
//                if (CommandBuilder.receiveCmd[add++] == CommandBuilder.WirelessProbeCMD.subcmdStart) {
//                    // 데이터 전송 시작 명령이므로 데이터 전송 스레드 시작.
//                    mSocketManagingClass.startGeneration();
//                    if (D) Log.d(TAG, "Start Sending Data");
//                } else {
//                    // 데이터 전송 중단 명령이므로 데이터 전송 스레드 종료.
//                    mSocketManagingClass.stopGeneration();
//                    if (D) Log.d(TAG, "Stop Sending Data");
//                };
                break;
        }
    }
}
