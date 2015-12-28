package org.homenet.woscilloscope;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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
    private MainActivity mCallerClone = null;
    Bitmap mBack;
    Pulse mPulse;
    SurfaceHolder mHolder;
    DrawThread mThread;
    Rect dstRect;
    private ScaleGestureDetector mScaleGestureDetector;

    public ScopePanel(Context context) {
        super(context);
        mCallerClone = (MainActivity)context;
        //mBack = BitmapFactory.decodeResource(context.getResources(), R.drawable.background);
        // 표면에 변화가 생길때의 이벤트를 처리할 콜백을 자신으로 지정한다.
        mHolder = getHolder();
        mHolder.addCallback(this);
        //
        mPulse = new Pulse();
        mPulse.initPulse();
        //
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);

    }
    // 배경 그리드를 그린다.
    private void prepareBackGroundGrid(int width, int height) {
        mBack = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas offscreen = new Canvas(mBack);
        offscreen.drawColor(Color.BLACK);
        Paint Pnt = new Paint();
        Pnt.setColor(Color.LTGRAY);

        // 외곽선 그리기
//        offscreen.drawLine(0, 0, width, height, Pnt);
        Pnt.setStrokeWidth(3.0f);
        Pnt.setStyle(Paint.Style.STROKE);
        offscreen.drawRect(0, 0, width-1, height-1, Pnt);
//        Pnt.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
//        Pnt.setPathEffect(new DashPathEffect(new float[] {10, 10 }, 3));
        Pnt.setPathEffect(new DashPathEffect(new float[] {2, 2 }, 0));

        Pnt.setStrokeWidth(1.5f);
        int incWidth = width / 10;
        int count = 0;
        for (int x = 0; x < width; x += incWidth) {
            if (count == 5) {
                Pnt.setStrokeWidth(5.0f);
                offscreen.drawLine(x, 0, x, height, Pnt);
                Pnt.setStrokeWidth(1.5f);
            }
            offscreen.drawLine(x, 0, x, height, Pnt);
            count++;
        }
        int incHeight = height / 10;
        count = 0;
        for (int y = 0; y < height-10; y += incHeight) {
            if (count == 5) {
                Pnt.setStrokeWidth(6.0f);
                offscreen.drawLine(0, y, width, y, Pnt);
                Pnt.setStrokeWidth(1.5f);
            }
            offscreen.drawLine(0, y, width, y, Pnt);
            count++;
        }
    }

    public void sendDrawMsg() {
        mThread.mHandler.sendEmptyMessage(ThreadMessage.SP_Draw);
    }

    // 표면이 생성될 때 그리기 스레드를 시작한다.
    public void surfaceCreated(SurfaceHolder holder) {
        int width = this.getWidth();
        int height = this.getHeight();
        prepareBackGroundGrid(width, height);
        mPulse.setWidth(width);
        mPulse.setHeight(height);
        dstRect = new Rect(0, 0, width-1, height-1); // 가로 모드일 때, 1920 * 1054 픽셀이 가용한 그리기 영역임.
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retVal = mScaleGestureDetector.onTouchEvent(event);
        return retVal || super.onTouchEvent(event);
    }

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
                    mCallerClone.orderCommandSending(CommandBuilder.WirelessProbeCMD.cmdSetHorizontalScale);
                }
            } else {
                // 가로이동이 유효한 크기 이하이면 무시.

            }
            if (Math.abs(distY) > 50.f) {
                if (Math.abs(distX) > 50.f) {
                    // 가로이동과 세로이동이 모두 유효한 크기이면 대각선 핀치 줌 동작으로 보고 무시
                } else {
                    // 세로이동만 유효한 크기이면 수직 스케일 동작으로 인식.
                    mCallerClone.orderCommandSending(CommandBuilder.WirelessProbeCMD.cmdSetVerticalScale);
                }
            } else {
                // 세로이동이 유효한 크기 이하이면 무시.
            }
            Log.d("ILineGraphView", "distX = " + distX + ", distY = " + distY);


        }
    };

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
//            prepareBackGroundGrid(mWidth, mHeight);
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
                canvas.translate(0, mHeight); // 좌상단 원점(0, 0)을 지정된 위치로 원점 이동

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