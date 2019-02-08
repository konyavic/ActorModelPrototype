package com.example.konyavic.testannotationprocessing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    MainStage stage;
    MainStageScenario scenario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger logger = new Logger() {
            @Override
            public void log(String message) {
                Log.d("test-annotation", message);
            }
        };
        stage = new MainStage();
        stage.mNetworkActor = new NetworkActor(logger);
        stage.mUploaderActor = new UploaderActor(123, logger);

        scenario = new MainStageScenario(stage);
        scenario.mUploaderCharacter.uploadFiles();
    }
}
