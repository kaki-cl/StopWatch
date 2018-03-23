package com.example.atuski.stopwatch.viewmodel;

import android.content.Context;
import android.databinding.ObservableField;
import android.util.Log;
import android.view.View;

import com.example.atuski.stopwatch.StopWatchApp;
import com.example.atuski.stopwatch.databinding.RxField;
import com.example.atuski.stopwatch.model.StopWatchModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.functions.Function;

public class MainViewModel{

    private final StopWatchModel stopWatchModel;

    // ストップウォッチの状態を更新＆通知するためにModelから共有されてるプロパティ群
    public ObservableField<String> formattedTime;
    public ObservableField<List<LapItem>> formattedLaps;

    public ObservableField<String> buttonLabel;
    public ObservableField<String> pauseButtonLabel;

    public MainViewModel(Context context) {
        Log.v("MainViewModel", "コンストラクタ");
        stopWatchModel = ((StopWatchApp)context).getStopWatchModel();


        formattedTime = new RxField<>(stopWatchModel.formattedTimeASObservable());
        formattedLaps = new RxField<>(stopWatchModel.formattedLapsASObservable()
                .map(new Function<List<String>, List<LapItem>>() {
                    @Override
                    public List<LapItem> apply(List<String> fLaps) throws Exception {
                        final List<LapItem> lapItems = new ArrayList<>();
                        int i = 1;
                        for (String lap : fLaps) {
                            lapItems.add(new LapItem(String.valueOf(i), lap));
                            i++;
                        }
                        return Collections.unmodifiableList(lapItems);
                    }
                })


        );


        buttonLabel = new RxField<>(stopWatchModel._buttonLabel);
        pauseButtonLabel = new RxField<>(stopWatchModel._pauseButtonLabel);
    }

    public void onClickStartOrStop(View view) {
        Log.v("MainViewModel", "onClickStartOrStop");
        stopWatchModel.startOrStop();
    }

    public void onClickPauseOrResume(View view) {
        Log.v("MainViewModel", "onClickPauseOrResume");
        stopWatchModel.pauseOrResume();
    }

    public void onClickLap(View view) {
        Log.v("MainViewModel", "onClickLap");
        stopWatchModel.lap();
    }
}
