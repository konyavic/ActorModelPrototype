package com.example.konyavic.testannotationprocessing;

import android.util.Log;

import com.example.konyavic.library.AbstractActor;
import com.example.konyavic.library.ActorClass;
import com.example.konyavic.library.ActorMethod;

import java.io.File;

@ActorClass
public class NetworkActor extends AbstractActor implements NetworkActorInterface {
    NetworkActor() {

    }

    @Override
    @ActorMethod
    public Boolean putFile(final String url, final File file) {
        // do something
        Log.d("testannotationprocessing", "NetworkActor#putFile " + Thread.currentThread().getName());
        Log.d("testannotationprocessing", "NetworkActor#putFile return");
        return true;
    }
}
