package abdoroid.quranradio.ui.main;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatDelegate;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.InterstitialAd;

import abdoroid.quranradio.R;
import abdoroid.quranradio.ui.favourites.FavouriteActivity;
import abdoroid.quranradio.ui.recordings.RecordsActivity;
import abdoroid.quranradio.ui.settings.SettingsActivity;
import abdoroid.quranradio.ui.splash.SplashScreen;
import abdoroid.quranradio.ui.stations.StationsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static boolean needReloaded = false;
    private Button stationsBtn, favBtn, settingBtn, recordsBtn;
    private InterstitialAd interstitialAd;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(MainActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdId(getString(R.string.interstitial_ad_id));
        loadInterstitialAd();

        ImageView background = findViewById(R.id.gifImageView);
        background.setBackgroundResource(R.drawable.main_background);
        AnimationDrawable animationDrawable = (AnimationDrawable) background.getBackground();
        animationDrawable.start();
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
        ActivityOptions options =
                ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
        if (stationsBtn.equals(v)) {
            showInterstitialAd();
            Intent stationIntent = new Intent(MainActivity.this, StationsActivity.class);
            startActivity(stationIntent, options.toBundle());
        } else if (favBtn.equals(v)) {
            showInterstitialAd();
            Intent favIntent = new Intent(MainActivity.this, FavouriteActivity.class);
            startActivity(favIntent, options.toBundle());
        } else if (recordsBtn.equals(v)) {
            showInterstitialAd();
            Intent recordsIntent = new Intent(MainActivity.this, RecordsActivity.class);
            startActivity(recordsIntent, options.toBundle());
        } else if (settingBtn.equals(v)) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
    }

    private void showInterstitialAd() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    private void loadInterstitialAd() {
        AdParam adParam = new AdParam.Builder().build();
        interstitialAd.loadAd(adParam);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (needReloaded) {
            finish();
            startActivity(new Intent(MainActivity.this, SplashScreen.class));
        }
        needReloaded = false;
    }
}