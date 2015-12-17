package org.homenet.woscilloscope;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by bbiggu on 2015. 11. 18..
 */
public class ScopePanel extends SurfaceView implements SurfaceHolder.Callback {
    // Debugging
    private final static String TAG = ScopePanel.class.getSimpleName();
    private static final boolean D = true;
    //
    Bitmap mBack;
    Pulse mPulse;
    SurfaceHolder mHolder;
    DrawThread mThread;
    Rect dstRect;

    public ScopePanel(Context context) {
        super(context);
        //mBack = BitmapFactory.decodeResource(context.getResources(), R.drawable.background);
        prepareBackGroundGrid();
        dstRect = new Rect(0, 0, 1919, 1053); // 가로 모드일 때, 1920 * 1054 픽셀이 가용한 그리기 영역임.
        // 표면에 변화가 생길때의 이벤트를 처리할 콜백을 자신으로 지정한다.
        mHolder = getHolder();
        mHolder.addCallback(this);
        //
        mPulse = new Pulse();
        mPulse.initPulse();
    }
    // 배경 그리드를 그린다.
    private void prepareBackGroundGrid() {
        mBack = Bitmap.createBitmap(1920, 1054, Bitmap.Config.ARGB_8888);
        Canvas offscreen = new Canvas(mBack);
        offscreen.drawColor(Color.LTGRAY);
        Paint Pnt = new Paint();
        Pnt.setColor(Color.WHITE);
        Pnt.setStrokeWidth(5.0f);
        for (int x = 0; x < 1920; x += 192) {
            offscreen.drawLine(x, 0, x, 1054, Pnt);
        }
        for (int y = 0; y < 1054; y += 105) {
            offscreen.drawLine(0, y, 1920, y, Pnt);
        }
    }

    public void sendDrawMsg() {
        mThread.mHandler.sendEmptyMessage(ThreadMessage.SP_Draw);
    }

    // 표면이 생성될 때 그리기 스레드를 시작한다.
    public void surfaceCreated(SurfaceHolder holder) {
        mThread = new DrawThread(mHolder);
        mThread.start();
    }

    // 표면이 파괴될 때 그리기를 중지한다.
    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread.quit();
        try { mThread.join(); } catch (InterruptedException e) {;}
    }

    // 표면의 크기가 바뀔 때 크기를 기록해 놓는다.
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mThread != null) {
            mThread.SizeChange(width, height);
        }
    }

    // 새로운 볼 생성
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            return true;
        }
        return false;
    }

    class DrawThread extends Thread {
        Handler mHandler;
        int mWidth, mHeight;
        SurfaceHolder mHolder;

        class QuitLooper implements Runnable {
            @Override
            public void run()
            {
                //
                Looper.myLooper().quit();
            }
        }

        DrawThread(SurfaceHolder Holder) {
            mHolder = Holder;
        }

        public void quit() {
            mHandler.post(new QuitLooper());
        }

        public void SizeChange(int Width, int Height) {
            mWidth = Width;
            mHeight = Height;
        }

        private void drawPulse() {
            Canvas canvas;

            // 그리기
            long start = System.nanoTime();
            synchronized(mHolder) {
                canvas = mHolder.lockCanvas();
                if (canvas == null) return;
                //canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(mBack, null, dstRect, null);

                if (mPulse != null) {
                    mPulse.makeData();
                    mPulse.Draw(canvas);
                }

                mHolder.unlockCanvasAndPost(canvas);
            }
            long end = System.nanoTime();
            if (D) Log.d(TAG, "Time of Drawing Process, Times = " + ((end - start) / 1000000) + "ms");

        }
        // 스레드에서 그리기를 수행한다.
        public void run() {
            drawPulse();
            Looper.prepare();
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case ThreadMessage.SP_Draw:	// draw
                            drawPulse();
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }
}