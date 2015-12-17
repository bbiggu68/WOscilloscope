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
    float data[]  = new float[10000];

    Paint mPnt = new Paint();
    Path mPath = new Path();

    void initPulse() {
//        mPnt.setAntiAlias(true);
        mPnt.setColor(Color.DKGRAY);
        mPnt.setStrokeWidth(30.0f);
        mPnt.setStyle(Paint.Style.STROKE);
//        mPnt.setStrokeWidth(3.0f);
    }

    void makeData() {
        byte[] rcvPulse = null;
        int sizeQueue = CommandBuilder.rcvCmdQueue.size();
        if ( sizeQueue > 0) {
            rcvPulse = CommandBuilder.rcvCmdQueue.poll();
//            for (int i = 0; i < (sizeQueue - 1); i++) {
//                CommandBuilder.rcvCmdQueue.poll();
//            }

            for(int i=0; i < 10000; i++){
                data[i] = (float) (rcvPulse[i] * -25);
            }
        }

        long start = System.nanoTime();
        mPath.reset();
        mPath.moveTo(0, data[0]);
        for (int l = 1; l < 10000; l++) {
            mPath.lineTo(l, data[l]);
        }
        long end = System.nanoTime();
        if (D) Log.d(TAG, "Time of Data Preparing, Times = " + ((end - start) / 1000000) + "ms");
    }

    void Draw(Canvas canvas) {
        canvas.save();
        canvas.translate(0, 527);
        canvas.scale(0.192f, 0.192f);
//        canvas.scale(1.92f, 1.f);
//        canvas.drawLines(lineSeries, mPnt);
        canvas.drawPath(mPath, mPnt);
//        float data[] = {0, 10, 100, 20, 100, 20, 300, 50, 300, 50, 500, 10};
//        canvas.drawLines(data, mPnt);
        canvas.restore();
    }
}
