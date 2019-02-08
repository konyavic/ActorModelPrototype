package com.example.konyavic.library;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractActorAdapter<T> {
    final protected ExecutorService mExecutorService;
    final private T mActor;

    public AbstractActorAdapter(T actor) {
        mActor = actor;
        mExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, mActor.getClass().getSimpleName());
            }
        });
    }

    public interface SyncedCall<T, V> {
        V run(T o);
    }

    protected <S> Future<S> syncFromAnotherActor(final SyncedCall<T, S> call) {
        return mExecutorService.submit(new Callable<S>() {
            // TODO: collect execptions
            @Override
            public S call() throws Exception {
                return call.run(mActor);
            }
        });
    }

    protected T getActor() {
        return mActor;
    }
}
