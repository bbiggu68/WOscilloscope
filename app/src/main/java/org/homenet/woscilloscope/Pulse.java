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

    float data[]  = new float[10000];
    float lineSeries[] = new float[39998];
    //    float data[]  = new float[1000];
//    float lineSeries[] = new float[3998];
    Paint mPnt = new Paint();
    Path mPath = new Path();

    void initPulse() {
        makeData();
//        mPnt.setAntiAlias(true);
        mPnt.setColor(Color.DKGRAY);
        mPnt.setStrokeWidth(30.0f);
        mPnt.setStyle(Paint.Style.STROKE);
//        mPnt.setStrokeWidth(3.0f);
    }

    void makeData() {
        double radian_angle = 0.0;
        double pitch = 5.f;

        for(int i=0; i < 10000; i++){
//        for(int i=0; i < 1000; i++){
//                data[i] = (float) (Math.sin(radian_angle) * -500);
            data[i] = (float) (Math.sin(radian_angle) * -2500);
            radian_angle += 2*Math.PI*pitch/10000;
//                radian_angle += 2*Math.PI*pitch/1000;
        }

        int k = 0;
        for (int j=0; j < 10000; j++) {
//        for (int j=0; j < 1000; j++) {
            lineSeries[k++] = j;
            lineSeries[k++] = data[j];
            if (j < 9999) {
//            if (j < 999) {
                lineSeries[k++] = j + 1;
                lineSeries[k++] = data[j + 1];
            }
        }
        long start = System.nanoTime();
        mPath.reset();
        mPath.moveTo(0, data[0]);
        for (int l = 1; l < 10000; l++) {
//        for (int l = 1; l < 1000; l++) {
            mPath.lineTo(l, data[l]);
        }
        long end = System.nanoTime();
        Log.w("Time of Data Preparing", "Times = " + ((end - start) / 1000000) + "ms");
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
