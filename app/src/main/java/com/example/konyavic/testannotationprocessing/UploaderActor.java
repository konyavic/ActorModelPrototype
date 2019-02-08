package com.example.konyavic.testannotationprocessing;

import com.example.konyavic.library.ActorClass;
import com.example.konyavic.library.ActorMethod;
import com.example.konyavic.library.StageActorRef;

import java.io.File;
import java.lang.ref.WeakReference;

@ActorClass
public class UploaderActor implements UploaderCharacter {
    private final Logger mLogger;
    private int mParam;

    @StageActorRef
    WeakReference<NetworkCharacter> networkCharacterRef = null;

    @StageActorRef(sync = true)
    WeakReference<NetworkCharacter> syncedNetworkCharacterRef = null;

    UploaderActor(int param, Logger logger) {
        mParam = param;
        mLogger = logger;
    }

    @Override
    @ActorMethod
    public void uploadFiles() {
        mLogger.log("UploaderActor#uploadFiles start");

        mLogger.log("UploaderActor#uploadFiles call async");
        NetworkCharacter networkCharacter = networkCharacterRef.get();
        networkCharacter.putFile("url", new File("name"));

        mLogger.log("UploaderActor#uploadFiles call sync");
        NetworkCharacter syncedNetworkCharacter = syncedNetworkCharacterRef.get();
        syncedNetworkCharacter.putFile("url", new File("name"));

        mLogger.log("UploaderActor#uploadFiles end");
    }
}
