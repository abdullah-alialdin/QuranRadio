package abdoroid.quranradio.ui.stations;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatDelegate;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.adapter.RadioAdapter;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.ui.recordings.RecordsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;


public class StationsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        Helper.setAnimation(this);
        setContentView(R.layout.activity_stations);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.radio_stations));
        ImageView toolbarImage = findViewById(R.id.toolbar_image);
        toolbarImage.setImageResource(R.drawable.ic_baseline_radio_24);
        LinearLayout noConnectionLayout = findViewById(R.id.no_connection_layout);
        Button noConnectionBtn = findViewById(R.id.no_connection_btn);
        noConnectionBtn.setOnClickListener(v -> {
            startActivity(new Intent(StationsActivity.this, RecordsActivity.class));
            finish();
        });
        ProgressBar progressBar = findViewById(R.id.my_progressBar);
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = this.getSharedPreferences("Abdullah", Context.MODE_PRIVATE);
        RadioStationsViewModel viewModel = new ViewModelProvider(this).get(RadioStationsViewModel.class);
        viewModel.getRadioStations();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        if (!Helper.isNetworkConnected(this)){
            noConnectionLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);
        }
        RadioAdapter adapter = new RadioAdapter(this, sharedPreferences);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        viewModel.radioStations.observe(this, radioDataModels -> {
            adapter.setRadiosList(radioDataModels);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Helper.reloadActivity){
            finish();
            startActivity(getIntent());
        }
        Helper.reloadActivity = false;
    }

}