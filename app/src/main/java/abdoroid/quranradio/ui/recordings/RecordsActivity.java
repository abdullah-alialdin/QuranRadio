package abdoroid.quranradio.ui.recordings;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import abdoroid.quranradio.R;
import abdoroid.quranradio.adapter.RecordsAdapter;
import abdoroid.quranradio.ui.main.MainActivity;
import abdoroid.quranradio.ui.stations.StationsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;
import abdoroid.quranradio.utils.StorageUtils;


public class RecordsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(RecordsActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        Helper.setAnimation(this);
        setContentView(R.layout.activity_records);

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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
        StorageUtils storageUtils = new StorageUtils(this);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecordsAdapter adapter = new RecordsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        if (storageUtils.loadRecordings().size() == 0){
            noConnectionLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}