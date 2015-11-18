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
    private final static String TAG = "MainActivity";
    private static final boolean D = true;
    private long mRcvCount = 0;
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
                    mRcvCount = 0;
                    mSocketManagingClass.sendCommand(CommandBuilder.WirelessProbeCMD.cmdStartStop, CommandBuilder.WirelessProbeCMD.subcmdStart);
                } else {
                    tmpButton.setText(getResources().getText(R.string.btnstart_caption));
                    mSocketManagingClass.sendCommand(CommandBuilder.WirelessProbeCMD.cmdStartStop, CommandBuilder.WirelessProbeCMD.subcmdStop);
                    if (D) Log.d(TAG, "Received Frame Count = " + mRcvCount);
                }

                break;
        }
    }
    private void processReceiveCommand() {
//        if (D) Log.d(TAG, "큐에 프레임이 몇개?" + CommandBuilder.rcvCmdQueue.size());
        parentApplication.endtime = System.nanoTime();
        if (D) Log.d(TAG, "Time of Data Receiving = " + ((parentApplication.endtime - parentApplication.starttime) / 1000000) + "ms");
        mRcvCount++;
        mScopeView.sendDrawMsg();
        try { Thread.sleep(50); } catch (InterruptedException e) {;}
        mSocketManagingClass.sendCommand(CommandBuilder.WirelessProbeCMD.cmdPulseReqest);
    }
}
