package com.commonlib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.commonlog.CLog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CLog.debug("MainActivity", "onCreate", "nothing");
    }
}
