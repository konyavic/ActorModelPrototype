package com.example.konyavic.testannotationprocessing;

import android.util.Log;

import com.example.konyavic.library.AbstractActor;
import com.example.konyavic.library.AbstractActorAdapter;
import com.example.konyavic.library.ActorClass;
import com.example.konyavic.library.ActorMethod;
import com.example.konyavic.library.ActorRef;

import java.io.File;
import java.util.concurrent.ExecutionException;

@ActorClass
public class UploaderActor extends AbstractActor implements UploaderActorInterface {
    private int mParam;

    @ActorRef
    NetworkActorInterface networkActor = null;

    UploaderActor(int param) {
        mParam = param;
    }

    @Override
    @ActorMethod
    public void uploadFiles() {
        Log.d("testannotationprocessing", "UploaderActor#uploadFiles " + Thread.currentThread().getName());

        MainStageInterface stage = (MainStageInterface) getStage();

        // putFile asynchronously
        Log.d("testannotationprocessing", "UploaderActor#uploadFiles async call NetworkActor");
        stage.getNetworkActor().putFile("url", new File("filename"));

        // putFile synchronously
        Log.d("testannotationprocessing", "UploaderActor#uploadFiles sync call NetworkActor");
        try {
            Boolean result = sync(stage.getNetworkActor(), new AbstractActorAdapter.SyncedCall<Boolean>() {
                @Override
                public Boolean run(Object o) {
                    NetworkActor networkAdapter = (NetworkActor) o;
                    return networkAdapter.putFile("url", new File("filename"));
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("testannotationprocessing", "UploaderActor#uploadFiles return");
    }
}
