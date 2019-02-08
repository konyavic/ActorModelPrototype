package com.example.konyavic.testannotationprocessing;

import org.junit.Test;

import java.io.File;
import java.lang.ref.WeakReference;

public class UploaderActorTest {
    @Test
    public void testUploadFiles() {
        UploaderActor actor = new UploaderActor(123, new Logger() {
            @Override
            public void log(String message) {
                // do nothing
            }
        });
        actor.networkCharacterRef = new WeakReference<NetworkCharacter>(new NetworkCharacter() {
            @Override
            public Boolean putFile(String url, File file) {
                // do nothing
                return null;
            }
        });
        actor.syncedNetworkCharacterRef = new WeakReference<NetworkCharacter>(new NetworkCharacter() {
            @Override
            public Boolean putFile(String url, File file) {
                // do nothing
                return null;
            }
        });

        actor.uploadFiles();
    }
}
