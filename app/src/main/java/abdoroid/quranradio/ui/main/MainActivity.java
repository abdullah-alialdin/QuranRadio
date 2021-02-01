package abdoroid.quranradio.ui.main;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

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
    private InterstitialAd mInterstitialAd;

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

        loadAd();

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
        if (stationsBtn.equals(v)) {
            Intent stationIntent = new Intent(MainActivity.this, StationsActivity.class);
            showAd(stationIntent);
        } else if (favBtn.equals(v)) {
            Intent favIntent = new Intent(MainActivity.this, FavouriteActivity.class);
            showAd(favIntent);
        } else if (recordsBtn.equals(v)) {
            Intent recordsIntent = new Intent(MainActivity.this, RecordsActivity.class);
            showAd(recordsIntent);
        } else if (settingBtn.equals(v)) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
    }

    private void showAd(Intent intent){

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                @Override
                public void onAdDismissedFullScreenContent() {
                    startActivity(intent);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    startActivity(intent);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    mInterstitialAd = null;
                    loadAd();
                }
            });
            mInterstitialAd.show(MainActivity.this);
        } else {
            startActivity(intent);
        }
    }

    private void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,getString(R.string.ad_interstitial_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }
        });
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