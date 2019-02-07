package com.example.konyavic.testannotationprocessing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainStage stage = new MainStage(new NetworkActorAdaptor(new NetworkActor()),
                new UploaderActorAdaptor(new UploaderActor(123)));
        stage.getUploaderActor().uploadFiles();
    }
}
