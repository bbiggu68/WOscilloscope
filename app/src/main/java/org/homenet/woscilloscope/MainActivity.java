package org.homenet.woscilloscope;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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

    //
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        handlerMain.removeCallbacks(mShowPart2Runnable);
        handlerMain.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        mVisible = true;
        // Schedule a runnable to display UI elements after a delay
        handlerMain.removeCallbacks(mHidePart2Runnable);
        handlerMain.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        handlerMain.removeCallbacks(mHideRunnable);
        handlerMain.postDelayed(mHideRunnable, delayMillis);
    }
    // ScopePanel에서 발생하는 제스쳐를 처리할 GestureDetector.SimpleOnGestureListener
    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onSingleTapConfirmed (MotionEvent e) {
            return true;
        }
        @Override
        public boolean onSingleTapUp (MotionEvent e) {
            toggle();
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    };
    // ScopePanel에서 발생하는 스케일 제스쳐를 처리할 ScaleGestureDetector.OnScaleGestureListener
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private float lastSpanX;
        private float lastSpanY;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            lastSpanX = scaleGestureDetector.getCurrentSpanX();
            lastSpanY = scaleGestureDetector.getCurrentSpanY();
            if (D) Log.d(TAG, "onScaleBegin : spanX = " + lastSpanX + ", spanY = " + lastSpanY);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
//            float spanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
//            float spanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
//
//            float distX = lastSpanX - spanX;
//            float distY = lastSpanY - spanY;
//
//            float newWidth = 0.0f;
//            float newHeight = 0.0f;
//
//            if (Math.abs(distX) > 5.f) {
//                if (Math.abs(distY) > 5.f) {
//                    newWidth = 1 * mCurrentViewport.width();
//                } else {
//                    newWidth = lastSpanX / spanX * mCurrentViewport.width();
//                }
//            } else {
//                newWidth = 1 * mCurrentViewport.width();
//            }
//            if (Math.abs(distY) > 5.f) {
//                if (Math.abs(distX) > 5.f) {
//                    newHeight = 1 * mCurrentViewport.height();
//                } else {
//                    newHeight = lastSpanY / spanY * mCurrentViewport.height();
//                }
//            } else {
//                newHeight = 1 * mCurrentViewport.height();
//            }
//
////            float newWidth = lastSpanX / spanX * mCurrentViewport.width();
////            float newHeight = lastSpanY / spanY * mCurrentViewport.height();
//
//            Log.d("ILineGraphView", "distX = " + distX + ", distY = " + distY + ", newWidth = " + newWidth + ", newHeight = " + newHeight);
//            float focusX = scaleGestureDetector.getFocusX();
//            float focusY = scaleGestureDetector.getFocusY();
//            hitTest(focusX, focusY, viewportFocus);
//
//            mCurrentViewport.set(
//                    viewportFocus.x
//                            - newWidth * (focusX - mContentRect.left)
//                            / mContentRect.width(),
//                    viewportFocus.y
//                            - newHeight * (mContentRect.bottom - focusY)
//                            / mContentRect.height(),
//                    0,
//                    0);
//            mCurrentViewport.right = mCurrentViewport.left + newWidth;
//            mCurrentViewport.bottom = mCurrentViewport.top + newHeight;
//            constrainViewport();
//            ViewCompat.postInvalidateOnAnimation(InteractiveLineGraphView.this);
//
//            lastSpanX = spanX;
//            lastSpanY = spanY;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

            float spanX = scaleGestureDetector.getCurrentSpanX();
            float spanY = scaleGestureDetector.getCurrentSpanY();
            if (D) Log.d(TAG, "onScaleEnd : spanX = " + spanX + ", spanY = " + spanY);

            float distX = lastSpanX - spanX;
            float distY = lastSpanY - spanY;

            if (Math.abs(distX) > 50.f) {
                if (Math.abs(distY) > 50.f) {
                    // 가로이동과 세로이동이 모두 유효한 크기이면 대각선 핀치 줌 동작으로 보고 무시
                } else {
                    // 가로이동만 유효한 크기이면 수평 스케일 동작으로 인식.
                    orderCommandSending(CommandBuilder.WirelessProbeCMD.cmdSetHorizontalScale);
                }
            } else {
                // 가로이동이 유효한 크기 이하이면 무시.

            }
            if (Math.abs(distY) > 50.f) {
                if (Math.abs(distX) > 50.f) {
                    // 가로이동과 세로이동이 모두 유효한 크기이면 대각선 핀치 줌 동작으로 보고 무시
                } else {
                    // 세로이동만 유효한 크기이면 수직 스케일 동작으로 인식.
                    orderCommandSending(CommandBuilder.WirelessProbeCMD.cmdSetVerticalScale);
                }
            } else {
                // 세로이동이 유효한 크기 이하이면 무시.
            }
            if (D) Log.d(TAG, "distX = " + distX + ", distY = " + distY);
        }
    };
    //
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
        mScopeView = new ScopePanel(this, mScaleGestureListener, mGestureListener);
        setContentView(R.layout.activity_main);
//        setContentView(mScopeView);
//        RelativeLayout vwMain = (RelativeLayout)findViewById(R.id.vwContainer);
        LinearLayout vwScope = (LinearLayout)findViewById(R.id.vwScope);
        vwScope.addView(mScopeView);

        handlerMain = new InnerHandler(MainActivity.this);
        parentApplication = (AppEntryPoint)getApplication();

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.vwScope);


//        // Set up the user interaction to manually show or hide the system UI.
////        mContentView.setOnClickListener(new View.OnClickListener() {
//        mScopeView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.btnStart).setOnTouchListener(mDelayHideTouchListener);
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
//        mSocketManagingClass = new SocketManager("255.255.255.255", 5000, this);
        mSocketManagingClass = new SocketManager("192.168.0.44", 50010, this);   // WP 하드웨어에 연결
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
