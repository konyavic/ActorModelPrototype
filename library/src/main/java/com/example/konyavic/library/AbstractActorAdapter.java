package com.example.konyavic.library;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractActorAdapter {
    final protected ExecutorService mExecutorService;
    final Object mActor;

    public AbstractActorAdapter(Object actor) {
        mActor = actor;
        mExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, mActor.getClass().getSimpleName());
            }
        });
    }

    public interface SyncedCall<T> {
        T run(Object o);
    }

    <T> Future<T> syncFromAnotherActor(final SyncedCall<T> call) {
        Future<T> result = mExecutorService.submit(new Callable<T>() {
            // TODO: collect execptions
            @Override
            public T call() throws Exception {
                return call.run(mActor);
            }
        });
        return result;
    }

    protected <T> T getActor(Class<T> cls) {
        return (T) mActor;
    }

    public void setStage(Object stage) {
        ((AbstractActor) mActor).mStage = new WeakReference<>(stage);
    }
}
