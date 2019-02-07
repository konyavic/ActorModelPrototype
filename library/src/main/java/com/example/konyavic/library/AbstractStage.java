package com.example.konyavic.library;

import java.util.concurrent.ExecutionException;

public abstract class AbstractStage {
    // TODO: deal with Object
    public <T> T sync(Object object, SyncedCall<T> call) throws ExecutionException, InterruptedException {
        AbstractActorAdapter adapter = (AbstractActorAdapter) object;
        return adapter.syncFromAnotherActor(call).get();
    }

    public interface SyncedCall<T> {
        T run(Object o);
    }
}
