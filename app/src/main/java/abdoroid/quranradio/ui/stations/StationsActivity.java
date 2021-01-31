package abdoroid.quranradio.ui.stations;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.adapter.RadioAdapter;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.ui.main.MainActivity;
import abdoroid.quranradio.ui.recordings.RecordsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;
import abdoroid.quranradio.utils.StorageUtils;


public class StationsActivity extends BaseActivity {

    private LinearLayout noConnectionLayout;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RadioAdapter adapter;
    private ArrayList<RadioDataModel> audioStations;
    private StorageUtils storageUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        Helper.setAnimation(StationsActivity.this);
        LocaleHelper.setLocale(StationsActivity.this);
        setContentView(R.layout.activity_stations);

        storageUtils = new StorageUtils(this);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.radio_stations));
        ImageView toolbarImage = findViewById(R.id.toolbar_image);
        toolbarImage.setImageResource(R.drawable.ic_baseline_radio);
        Toolbar toolbar = findViewById(R.id.radio_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        noConnectionLayout = findViewById(R.id.no_connection_layout);
        Button noConnectionBtn = findViewById(R.id.no_connection_btn);

        noConnectionBtn.setOnClickListener(v -> {
            startActivity(new Intent(StationsActivity.this, RecordsActivity.class));
            finish();
        });

        progressBar = findViewById(R.id.my_progressBar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recycler_view);

        checkConnectionAndSetupViews();

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            checkConnectionAndSetupViews();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setAudioStations() {
        RadioStationsViewModel viewModel = new ViewModelProvider(this).get(RadioStationsViewModel.class);
        viewModel.getRadioStations();
        viewModel.radioStations.observe(this, radioDataModels -> {
            audioStations = new ArrayList<>(radioDataModels);
            adapter = new RadioAdapter(this, storageUtils.STATION_PLAYER, audioStations);
            adapter.notifyDataSetChanged();
            setUpRecyclerView();
        });
    }

    private void setUpRecyclerView(){
        noConnectionLayout.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }

    private void setUpNoConnectionLayout(){
        noConnectionLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void checkConnectionAndSetupViews(){
        if (Helper.isNetworkConnected(this)){
            setUpNoConnectionLayout();
        } else {
            setAudioStations();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}