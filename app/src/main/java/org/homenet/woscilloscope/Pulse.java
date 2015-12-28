package org.homenet.woscilloscope;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

/**
 * Created by bbiggu on 2015. 11. 18..
 */
public class Pulse {
    // Debugging
    private final static String TAG = Pulse.class.getSimpleName();
    private static final boolean D = true;
    //
    private final static int FRAME_SIZE = 1000;
    float data[]  = new float[FRAME_SIZE];
    //
    int mwidth = 0;
    int mHeight = 0;
    float mWidthScale = 0.0f;
    float mHeightScale = 0.0f;

    Paint mPnt = new Paint();
    Path mPath = new Path();

    void initPulse() {
//        mPnt.setAntiAlias(true);
        mPnt.setColor(Color.YELLOW);
//        mPnt.setStrokeWidth(30.0f);
        mPnt.setStrokeWidth(5.0f);
        mPnt.setStyle(Paint.Style.STROKE);
//        mPnt.setStrokeWidth(3.0f);
    }

    void setWidth(int width) {
        this.mwidth = width;
        this.mWidthScale = ((float)width / FRAME_SIZE); // 프레임 사이즈
    }
    void setHeight(int height) {
        this.mHeight = height;
        this.mHeightScale = ((float)height / 256); // 데이터의 해상도
    }
    void makeData() {
        byte[] rcvPulse = null;
        int sizeQueue = CommandBuilder.rcvCmdQueue.size();
        if ( sizeQueue > 0) {
            rcvPulse = CommandBuilder.rcvCmdQueue.poll();
//            for (int i = 0; i < (sizeQueue - 1); i++) {
//                CommandBuilder.rcvCmdQueue.poll();
//            }

            for(int i=0; i < FRAME_SIZE; i++){
//                data[i] = (float) (rcvPulse[i] * -25);
                data[i] = (float) (rcvPulse[i] * -mHeightScale); // 세로 비율 ( 1200or900 / 256 = 3.6) 만큼씩 곱해줘야 256 단계의 값을 1200or900 픽셀에 모두 그릴 수 있다.
            }
        }

        long start = System.nanoTime();
        mPath.reset();
        mPath.moveTo(0, data[0]);
        for (int l = 1; l < FRAME_SIZE; l++) {
            mPath.lineTo((float)(l*mWidthScale), data[l]); // 가로 비율 (1920 / 1000 = 1.92) 만큼씩 곱해줘야 1000개를 1920 픽셀에 모두 그릴 수 있다.
        }
        long end = System.nanoTime();
        if (D) Log.d(TAG, "Time of Data Preparing, Times = " + ((end - start) / 1000000) + "ms");
    }

    void Draw(Canvas canvas) {
//        canvas.save();
//        canvas.translate(0, 527);
//        canvas.translate(0, mHeight); // 좌상단 원점(0, 0)을 지정된 위치로 원점 이동
//        canvas.scale(1.f, 1.f);
//        canvas.drawLines(lineSeries, mPnt);
        canvas.drawPath(mPath, mPnt);
//        float data[] = {0, 10, 100, 20, 100, 20, 300, 50, 300, 50, 500, 10};
//        canvas.drawLines(data, mPnt);
//        canvas.restore();
    }
}
