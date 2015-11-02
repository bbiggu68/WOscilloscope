package org.homenet.woscilloscope;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    // Debugging
    private final static String TAG = "MainActivity";
    private static final boolean D = true;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
