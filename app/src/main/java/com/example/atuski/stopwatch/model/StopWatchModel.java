package com.example.atuski.stopwatch.model;

import android.util.Log;

import org.reactivestreams.Subscription;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class StopWatchModel implements Subscription {

    //ストップウォッチ主要プロパティ
    private BehaviorSubject<Long> time = BehaviorSubject.createDefault(0L);
    private BehaviorSubject<Boolean> isRunnning = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> isPaused = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> isVisibleMillis = BehaviorSubject.createDefault(true);
    private BehaviorSubject<List<Long>> laps = BehaviorSubject.createDefault(Collections.emptyList());

    //スレッドを超えて値を更新するため
    private Subject<Long> timeSerialized = time.toSerialized();
    private Subject<Boolean> isRunnningSerialized = isRunnning.toSerialized();
    private Subject<Boolean> isPausedSerialized = isPaused.toSerialized();


    //todo serializedじゃなくていいかも。やっぱりそのほうがいいかも。
    private BehaviorSubject<String> buttonLabel = BehaviorSubject.createDefault("Start");
    private Subject<String> buttonLabelSerialized = buttonLabel.toSerialized();
    private BehaviorSubject<String> pauseButtonLabel = BehaviorSubject.createDefault("Pause");
    private Subject<String> pauseButtonLabelSerialized = pauseButtonLabel.toSerialized();

    //公開用プロパティ
    public final Observable<String> _buttonLabel = buttonLabel;
    public final Observable<String> _pauseButtonLabel = pauseButtonLabel;


    //計測に用いる時間系の補助プロパティ
    private AtomicLong startTime = new AtomicLong(0L);
    private Long pausedTime = null;

    //購読停止用のDisposable
    private Disposable _timerDisposable = null;

    public Observable<String> formattedTimeASObservable() {

        return Observable
                .combineLatest(timeSerialized, isVisibleMillis, new BiFunction<Long, Boolean, String>() {
                    @Override
                    public String apply(Long time, Boolean isVisibleMillis) throws Exception {
                        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS", Locale.getDefault());
                        return sdf.format(new Date(time));
                    }
                });
    }

    public Observable<List<String>> formattedLapsASObservable() {

        return Observable
                .combineLatest(laps, isVisibleMillis, new BiFunction<List<Long>, Boolean, List<String>>() {
                    @Override
                    public List<String> apply(List<Long> laps, Boolean isVisibleMillis) throws Exception {
                        final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS", Locale.getDefault());

                        final List<String> formattedLaps = new ArrayList<>();
                        for (Long lap : laps) {
                            formattedLaps.add(sdf.format(new Date(lap)));
                        }

                        return Collections.unmodifiableList(formattedLaps);
                    }
                });
    }

    public void startOrStop() {
        if (!isRunnning.getValue()) {
            start();
        } else {
            stop();
        }
    }

    public void pauseOrResume() {

        if (!isPaused.getValue()) {
            // 停止する。
            pause();
        } else {
            // 再開する。
            resume();
        }
    }

    public void lap() {

        // 要はlapを一個追加した上でコレクションの更新を通知している。
        // serializedを使用しているので、コレクションを上書きするだけという感じ

        final List<Long> lapList = laps.getValue();
        final List<Long> newLaps = new ArrayList<>();

        newLaps.addAll(lapList);

        long totalLap = 0L;
        for (Long lap:lapList) {
            totalLap += lap;
        }

        final long lap = System.currentTimeMillis() - startTime.get();
        newLaps.add(lap - totalLap);

        laps.onNext(Collections.unmodifiableList(newLaps));
    }

    private void start() {

        buttonLabelSerialized.onNext("Finish");

        _timerDisposable =
                Observable
                        .interval(1, TimeUnit.MILLISECONDS, Schedulers.newThread())
                        .compose(new ObservableTransformer<Long, Long>() {
                            @Override
                            public ObservableSource<Long> apply(Observable<Long> upstream) {

                                isRunnningSerialized.onNext(true);
                                startTime.set(System.currentTimeMillis());
                                return upstream;
                            }
                        })
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                // タイマー値を通知
                                timeSerialized.onNext(System.currentTimeMillis() - startTime.get());
                            }
                        });
    }

    private void stop() {
        if (_timerDisposable != null) {
            _timerDisposable.dispose();
            _timerDisposable = null; //
        }
        isRunnningSerialized.onNext(false);
        buttonLabelSerialized.onNext("Finish");
    }

    private void pause() {
        isPausedSerialized.onNext(true);
        pauseButtonLabelSerialized.onNext("Resume");

        if (_timerDisposable != null) {
            pausedTime = time.getValue();
            _timerDisposable.dispose();
        }
    }

    private void resume() {
        isPausedSerialized.onNext(false);
        pauseButtonLabelSerialized.onNext("Pasue");

        _timerDisposable =
                Observable
                        .interval(1, TimeUnit.MILLISECONDS, Schedulers.newThread())
                        .compose(new ObservableTransformer<Long, Long>() {
                            @Override
                            public ObservableSource<Long> apply(Observable<Long> upstream) {

                                startTime.set(System.currentTimeMillis());
                                return upstream;
                            }
                        })
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                // タイマー値を通知
                                timeSerialized.onNext(System.currentTimeMillis() - startTime.get() + pausedTime);
                            }
                        });
    }


    @Override
    public void request(long n) {

    }

    @Override
    public void cancel() {

    }
}
