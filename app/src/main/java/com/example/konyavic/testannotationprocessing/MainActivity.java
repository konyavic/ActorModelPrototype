package com.example.konyavic.testannotationprocessing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("testannotationprocessing", MyClassGenerated.hello());

        UploaderModule uploader = new UploaderModuleImpl();
        uploader.startForegroundLoop(1, "asdf");

        UploaderModule uploader2 = new UploaderModuleActor(new UploaderModuleImpl());
        uploader2.startForegroundLoop(2, "asdf");
    }
}
