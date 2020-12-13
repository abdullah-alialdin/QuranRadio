package abdoroid.quranradio.ui.favourites;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.adapter.RadioAdapter;
import abdoroid.quranradio.ui.recordings.RecordsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;

public class FavouriteActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LocaleHelper.setLocale(FavouriteActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        Helper.setAnimation(this);
        setContentView(R.layout.activity_favourite);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.favourites));
        ImageView toolbarImage = findViewById(R.id.toolbar_image);
        toolbarImage.setImageResource(R.drawable.ic_baseline_favorite_white);
        LinearLayout noConnectionLayout = findViewById(R.id.no_connection_layout);
        Button noConnectionBtn = findViewById(R.id.no_connection_btn);
        noConnectionBtn.setOnClickListener(v -> {
                startActivity(new Intent(FavouriteActivity.this, RecordsActivity.class));
                finish();
        });
        ProgressBar progressBar = findViewById(R.id.my_progressBar);
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = this.getSharedPreferences("Abdullah", Context.MODE_PRIVATE);
        FavouriteViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.
                getInstance(this.getApplication())).get(FavouriteViewModel.class);
        viewModel.getFavouriteStations();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        if (!Helper.isNetworkConnected(this)){
            noConnectionLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);
        }
        TextView noFavTxt =findViewById(R.id.no_fav_text);
        RadioAdapter adapter = new RadioAdapter(this, sharedPreferences);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        viewModel.radioStations.observe(this, radioDataModels -> {
            if (radioDataModels.size() == 0){
                noConnectionLayout.setVisibility(View.VISIBLE);
                noFavTxt.setText(R.string.no_fav);
                recyclerView.setVisibility(View.INVISIBLE);
            }
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