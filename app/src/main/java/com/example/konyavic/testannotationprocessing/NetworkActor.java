package com.example.konyavic.testannotationprocessing;

import com.example.konyavic.library.ActorClass;
import com.example.konyavic.library.ActorMethod;

import java.io.File;

@ActorClass
public class NetworkActor implements NetworkCharacter {
    private final Logger mLogger;

    NetworkActor(Logger logger) {
        mLogger = logger;
    }

    @Override
    @ActorMethod
    public Boolean putFile(final String url, final File file) {
        // do something
        mLogger.log("NetworkActor#putFile start");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mLogger.log("NetworkActor#putFile end");
        return true;
    }
}
