package com.example.konyavic.library;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

public abstract class AbstractActor {
    WeakReference<AbstractStage> mStage;

    protected AbstractStage getStage() {
        return mStage.get();
    }

    protected <T> T sync(Object actor, AbstractStage.SyncedCall<T> call) throws ExecutionException, InterruptedException {
        return getStage().sync(actor, call);
    }
}