package com.example.atuski.stopwatch.databinding;

import android.databinding.ObservableField;
import android.util.Log;

import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class RxField<T> extends ObservableField<T> {

    private final Observable<T> observable;

    //subscriptionでいこうとしたら、addOnPropertyChangedCallbackメソッドでうまくいかなかった。
    private final Map<Integer, Subscription> subscriptionMap = new HashMap<Integer, Subscription>();

    private final Map<Integer, Disposable> disposableMap = new HashMap<Integer, Disposable>();

    public RxField(Observable<T> observable) {
        Log.v("RxField", "コンストラクタ");
        this.observable = observable;
    }

    public RxField(Observable<T> observable, T defaultValue) {
        super(defaultValue);
        Log.v("RxField", "RxFieldRxFieldRxFieldRxField");
        this.observable = observable;
        Log.v("RxField", "");
    }


    @Override
    public synchronized void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        super.addOnPropertyChangedCallback(callback);
        Log.v("RxField", "addOnPropertyChangedCallback開始");

        disposableMap.put(callback.hashCode(), observable.subscribe(new Consumer<T>() {
            @Override
            public void accept(T value) throws Exception {
                Log.v("RxField", "value");
                set(value);
            }
        }));

        Log.v("RxField", "addOnPropertyChangedCallback終了");
    }

    @Override
    public synchronized void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {

        if (disposableMap.containsKey(callback.hashCode())) {
            final Disposable disposable = disposableMap.get(callback.hashCode());
            disposable.dispose();
            disposableMap.remove(callback.hashCode());
        }
        super.removeOnPropertyChangedCallback(callback);
    }


    @Override
    public void set(T value) {
        Log.v("RxField set", "set開始");
        Log.v("RxField set", value.getClass().toString());
        Log.v("RxField set", value.toString());
        super.set(value);
        Log.v("RxField set", "set終了");
    }


}
