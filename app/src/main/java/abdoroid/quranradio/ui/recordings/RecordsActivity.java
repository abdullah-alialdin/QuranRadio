package abdoroid.quranradio.ui.recordings;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import abdoroid.quranradio.R;
import abdoroid.quranradio.adapter.RecordsAdapter;
import abdoroid.quranradio.ui.stations.StationsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;


public class RecordsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(RecordsActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        Helper.setAnimation(this);
        setContentView(R.layout.activity_records);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.recordings));
        ImageView toolbarImage = findViewById(R.id.toolbar_image);
        toolbarImage.setImageResource(R.drawable.ic_baseline_record);
        LinearLayout noConnectionLayout = findViewById(R.id.no_connection_layout);
        Button noConnectionBtn = findViewById(R.id.no_connection_btn);
        noConnectionBtn.setOnClickListener(v -> {
            startActivity(new Intent(RecordsActivity.this, StationsActivity.class));
            finish();
        });
        ProgressBar progressBar = findViewById(R.id.my_progressBar);
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = this.getSharedPreferences("RecordPreferences", Context.MODE_PRIVATE);
        RecordsViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.
                getInstance(this.getApplication())).get(RecordsViewModel.class);
        viewModel.getRecordedStations();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecordsAdapter adapter = new RecordsAdapter(this, sharedPreferences);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        viewModel.recordings.observe(this, radioDataModels -> {
            if (radioDataModels.size() == 0){
                progressBar.setVisibility(View.GONE);
                noConnectionLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
            }else{
                adapter.setRadiosList(radioDataModels);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}