package abdoroid.quranradio.ui.stations;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatDelegate;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import abdoroid.quranradio.R;
import abdoroid.quranradio.adapter.RadioAdapter;
import abdoroid.quranradio.ui.main.MainActivity;
import abdoroid.quranradio.ui.recordings.RecordsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;
import abdoroid.quranradio.utils.StorageUtils;


public class StationsActivity extends BaseActivity {

    private LinearLayout noConnectionLayout;
    private ProgressBar progressBar;
    private RadioStationsViewModel viewModel;
    private RecyclerView recyclerView;
    private RadioAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        Helper.setAnimation(StationsActivity.this);
        LocaleHelper.setLocale(StationsActivity.this);
        setContentView(R.layout.activity_stations);

        StorageUtils storageUtils = new StorageUtils(this);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.radio_stations));
        ImageView toolbarImage = findViewById(R.id.toolbar_image);
        toolbarImage.setImageResource(R.drawable.ic_baseline_radio);

        noConnectionLayout = findViewById(R.id.no_connection_layout);
        Button noConnectionBtn = findViewById(R.id.no_connection_btn);

        noConnectionBtn.setOnClickListener(v -> {
            startActivity(new Intent(StationsActivity.this, RecordsActivity.class));
            finish();
        });

        progressBar = findViewById(R.id.my_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.recycler_view);
        adapter = new RadioAdapter(this, storageUtils.STATION_PLAYER);
        viewModel = new ViewModelProvider(this).get(RadioStationsViewModel.class);

        checkConnectionAndSetupViews();

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            checkConnectionAndSetupViews();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setUpRecyclerView(){
        noConnectionLayout.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        viewModel.getRadioStations();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        viewModel.radioStations.observe(this, radioDataModels -> {
            adapter.setRadiosList(radioDataModels);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void setUpNoConnectionLayout(){
        noConnectionLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void checkConnectionAndSetupViews(){
        if (Helper.isNetworkConnected(this)){
            setUpNoConnectionLayout();
        } else {
            setUpRecyclerView();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}