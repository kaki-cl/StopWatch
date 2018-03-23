package com.example.atuski.stopwatch;

import android.app.Application;

import com.example.atuski.stopwatch.model.StopWatchModel;


public class StopWatchApp extends Application {

    private final StopWatchModel stopWatchModel = new StopWatchModel();

    public StopWatchModel getStopWatchModel() {
        return stopWatchModel;
    }

    @Override
    public void onTerminate() {
        stopWatchModel.cancel();
        super.onTerminate();
    }
}
