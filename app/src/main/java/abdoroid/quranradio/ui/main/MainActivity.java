package abdoroid.quranradio.ui.main;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

import abdoroid.quranradio.R;
import abdoroid.quranradio.ui.favourites.FavouriteActivity;
import abdoroid.quranradio.ui.recordings.RecordsActivity;
import abdoroid.quranradio.ui.settings.SettingsActivity;
import abdoroid.quranradio.ui.stations.StationsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button stationsBtn, favBtn, settingBtn, recordsBtn;
    public static boolean needReloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LocaleHelper.setLocale(MainActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        setContentView(R.layout.activity_main);

        stationsBtn = findViewById(R.id.radio_stations_btn);
        stationsBtn.setOnClickListener(this);

        favBtn = findViewById(R.id.favourites_btn);
        favBtn.setOnClickListener(this);

        recordsBtn = findViewById(R.id.records_btn);
        recordsBtn.setOnClickListener(this);

        settingBtn = findViewById(R.id.settings_btn);
        settingBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        ActivityOptions options =
                ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
        switch (id){
            case R.id.radio_stations_btn:
                Intent stationIntent = new Intent(MainActivity.this, StationsActivity.class);
                startActivity(stationIntent, options.toBundle());
                break;
            case R.id.favourites_btn:
                Intent favIntent = new Intent(MainActivity.this, FavouriteActivity.class);
                startActivity(favIntent, options.toBundle());
                break;
            case R.id.records_btn:
                Intent recordsIntent = new Intent(MainActivity.this, RecordsActivity.class);
                startActivity(recordsIntent, options.toBundle());
                break;
            case R.id.settings_btn:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needReloaded){
            finish();
            startActivity(getIntent());
        }
        needReloaded = false;
    }
}