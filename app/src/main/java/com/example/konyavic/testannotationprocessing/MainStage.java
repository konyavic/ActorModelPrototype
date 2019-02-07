package com.example.konyavic.testannotationprocessing;

import com.example.konyavic.library.AbstractActorAdapter;

public class MainStage implements MainStageInterface{
    private NetworkActorInterface mNetworkActor;
    private UploaderActorInterface mUploaderActor;

    public MainStage(NetworkActorInterface networkActor, UploaderActorInterface uploader) {
        mNetworkActor = networkActor;
        mUploaderActor = uploader;

        ((AbstractActorAdapter)mNetworkActor).setStage(this);
        ((AbstractActorAdapter)mUploaderActor).setStage(this);
    }

    @Override
    public NetworkActorInterface getNetworkActor() {
        return mNetworkActor;
    }

    @Override
    public UploaderActorInterface getUploaderActor() {
        return mUploaderActor;
    }
}
