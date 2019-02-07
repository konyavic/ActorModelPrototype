package com.example.konyavic.library;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

public abstract class AbstractActor {
    WeakReference<Object> mStage;

    protected Object getStage() {
        return mStage.get();
    }

    protected <T> T sync(Object actor, AbstractActorAdapter.SyncedCall<T> call) throws ExecutionException, InterruptedException {
        return ((AbstractActorAdapter)actor).syncFromAnotherActor(call).get();
    }
}