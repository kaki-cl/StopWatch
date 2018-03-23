package com.example.atuski.stopwatch;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.atuski.stopwatch.viewmodel.MainViewModel;
import com.example.atuski.stopwatch.databinding.ActivityMainBinding;
import com.example.atuski.stopwatch.views.adapters.LapAdapter;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = new MainViewModel(this.getApplicationContext());
        binding.setViewModel(viewModel);
    }

    /**
     * ListView と ViewModel のカスタムバインディング
     *
     * TODO 本当は viewModel.formattedLaps とバインドしたい
     */
    @BindingAdapter("formattedLaps")
    public static void setFormattedLaps(ListView listView, final MainViewModel viewModel) {
        final LapAdapter adapter = new LapAdapter(listView.getContext());
        listView.setAdapter(adapter);

        Log.v("MainActivity", "008.5");


        viewModel.formattedLaps.addOnPropertyChangedCallback(new android.databinding.Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(android.databinding.Observable sender, int propertyId) {

                Log.v("MainActivity", "011");
                Log.v("MainActivity", "onPropertyChanged");
                adapter.clear();

                Log.v("MainActivity", "viewModel.formattedLaps.get()はList<LapItem>を返してる");
                adapter.addAll(viewModel.formattedLaps.get());
            }
        });

        // バインド時に値を更新(バインド時なので初回のみ)
        adapter.clear();
        adapter.addAll(viewModel.formattedLaps.get());
    }

}
