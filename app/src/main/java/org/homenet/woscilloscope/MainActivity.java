package org.homenet.woscilloscope;

import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    // Debugging
    private final static String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true;
    private long mRcvCount = 0;
    private boolean mResponseFlag = false;
    //
    public AppEntryPoint parentApplication = null;
    private SocketManager mSocketManagingClass = null;
    private Thread mThread4Socket = null;
    private ScopePanel mScopeView;
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
                case ThreadMessage.MA_SEND_HSCALE_CMD:
                    theMain.processReceiveCommand();
                    break;
                case ThreadMessage.MA_SEND_VSCALE_CMD:
                    theMain.processReceiveCommand();
                    break;
//                case ThreadMessage.MA_CHECK_RESPONSE:
//                    theMain.checkRequestAndResponse();
//                    break;
            }
        }
    }
    //==========================================
    // Activity Callback
    //==========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScopeView = new ScopePanel(this);
        setContentView(R.layout.activity_main);
//        setContentView(mScopeView);
//        RelativeLayout vwMain = (RelativeLayout)findViewById(R.id.vwContainer);
        LinearLayout vwScope = (LinearLayout)findViewById(R.id.vwScope);
        vwScope.addView(mScopeView);

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
        createSocketManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroySocketManager();
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
            case R.id.btnStart:
                Button tmpButton = (Button)v;

                if (tmpButton.getText().equals(getResources().getText(R.string.btnstart_caption))) {
                    tmpButton.setText(getResources().getText(R.string.btnstop_caption));
                    mRcvCount = 0;
                    mSocketManagingClass.startProbe();
                } else {
                    tmpButton.setText(getResources().getText(R.string.btnstart_caption));
                    mSocketManagingClass.stopProbe();
                    if (D) Log.d(TAG, "Received Frame Count = " + mRcvCount);
                    if (D) Log.d(TAG, "큐에 남아있는 프레임이 몇개?" + CommandBuilder.rcvCmdQueue.size());
                }

                break;
        }
    }

    public void orderCommandSending(byte cmd) {
        mSocketManagingClass.sendCommand(cmd);
    }

    private void createSocketManager() {
        // Start SocketManager Thread
//        mSocketManagingClass = new SocketManager("192.168.0.11", 5000, this);
        mSocketManagingClass = new SocketManager("255.255.255.255", 5000, this);
//                mSocketManagingClass = new SocketManager("192.168.0.5", 5000, this);
        mThread4Socket = new Thread(mSocketManagingClass, "SocketMgr");
        mThread4Socket.setDaemon(true); // UI 스레드가 가면 같이 간다.
        mThread4Socket.start();
        // 스레드 start()하고 조금 기다려야 스레드의 run()이 실행되서 핸들러가 만들어진다.
        try { Thread.sleep(50); } catch (InterruptedException e) {;}
        mSocketManagingClass.connect();
    }

    private void destroySocketManager() {
        mSocketManagingClass.disconnect();
        if (mThread4Socket.isAlive()) {
            mSocketManagingClass.quit();
        }
    }

    private void processReceiveCommand() {
        mResponseFlag = true;
//        if (D) Log.d(TAG, "큐에 프레임이 몇개?" + CommandBuilder.rcvCmdQueue.size());
        parentApplication.endtime = System.nanoTime();
        if (D) Log.d(TAG, "Time of Data Receiving = " + ((parentApplication.endtime - parentApplication.starttime) / 1000000) + "ms");
        mRcvCount++;
        // 화면 그리기 메시지를 DrawThread에 보내면 평균적인 화면 그리기 시간인 50ms 정도가 소요된다.
        mScopeView.sendDrawMsg();
        // 50ms 정도의 화면 그리기가 수행완료되기 전에 데이터 요청을 보내면 평균적인 수신 시간이 1ms 정도이므로 화면에 그려야 할 데이터가 메모리가 넘치도록 쏟아져 들어오므로
        // 평균 그리기 시간 정도 대기를(Thread.sleep(50)) 했다가 데이터 요청을 보내도록 하였다.
//        try { Thread.sleep(50); } catch (InterruptedException e) {;}
//        mSocketManagingClass.sendCommand(CommandBuilder.WirelessProbeCMD.cmdPulseReqest);
//        mResponseFlag = false;
//        handlerMain.sendEmptyMessageDelayed(ThreadMessage.MA_CHECK_RESPONSE, 1000);
    }

//    private void checkRequestAndResponse() {
//        if (!mResponseFlag) {
//            mSocketManagingClass.sendCommand(CommandBuilder.WirelessProbeCMD.cmdPulseReqest);
//        }
//    }
}
