package com.example.konyavic.testannotationprocessing;

import android.util.Log;

import com.example.konyavic.library.Actor;
import com.example.konyavic.library.ActorMethod;

@Actor(name = "UploaderModuleActor", implementing = "com.example.konyavic.testannotationprocessing.UploaderModule")
public class UploaderModuleImpl implements UploaderModule {

    UploaderModuleImpl() { }

    @ActorMethod
    public void startForegroundLoop(final int dummyArg1, final String dummyArg2) {
        Log.d("UploaderModule", "my thread is " + Thread.currentThread().getName());
    }

    private String aPrivateMethod() {
        return "foo";
    }
}
