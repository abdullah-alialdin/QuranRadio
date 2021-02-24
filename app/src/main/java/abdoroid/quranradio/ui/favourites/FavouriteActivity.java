package abdoroid.quranradio.ui.favourites;

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

public class FavouriteActivity extends BaseActivity {

    private FavouriteViewModel viewModel;
    private RecyclerView recyclerView;
    private RadioAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout noConnectionLayout;
    private TextView noFavTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(FavouriteActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        Helper.setAnimation(FavouriteActivity.this);
        setContentView(R.layout.activity_favourite);

        StorageUtils storageUtils = new StorageUtils(this);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.favourites));
        ImageView toolbarImage = findViewById(R.id.toolbar_image);
        toolbarImage.setImageResource(R.drawable.ic_baseline_favorite_white);

        noConnectionLayout = findViewById(R.id.no_connection_layout);
        noFavTxt = findViewById(R.id.no_fav_text);
        Button noConnectionBtn = findViewById(R.id.no_connection_btn);
        noConnectionBtn.setOnClickListener(v -> {
                startActivity(new Intent(FavouriteActivity.this, RecordsActivity.class));
                finish();
        });
        progressBar = findViewById(R.id.my_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.recycler_view);
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.
                getInstance(this.getApplication())).get(FavouriteViewModel.class);
        adapter = new RadioAdapter(this, storageUtils.FAVOURITES_PLAYER);

        checkConnectionAndSetLayout();
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            checkConnectionAndSetLayout();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setRecyclerView(){
        noConnectionLayout.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        viewModel.getFavouriteStations();
        viewModel.radioStations.observe(this, radioDataModels -> {
            if (radioDataModels.size() == 0){
                setNoConnectionLayout();
                noFavTxt.setText(R.string.no_fav);
            }
            adapter.setRadiosList(radioDataModels);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void setNoConnectionLayout(){
        noConnectionLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        noFavTxt.setText(R.string.no_internet);
    }

    private void checkConnectionAndSetLayout(){
        if (Helper.isNetworkConnected(this)){
            setNoConnectionLayout();
        }else {
            setRecyclerView();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}