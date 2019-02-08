package com.example.konyavic.testannotationprocessing;

import com.example.konyavic.library.StageActor;
import com.example.konyavic.library.StageClass;

@StageClass
public class MainStage {
    @StageActor(playing = "com.example.konyavic.testannotationprocessing.NetworkCharacter")
    NetworkActor mNetworkActor;

    @StageActor(playing = "com.example.konyavic.testannotationprocessing.UploaderCharacter")
    UploaderActor mUploaderActor;
}
