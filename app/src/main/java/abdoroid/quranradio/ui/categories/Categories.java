package abdoroid.quranradio.ui.categories;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.adapter.CategoriesAdapter;
import abdoroid.quranradio.pojo.CategoriesDataModel;
import abdoroid.quranradio.ui.main.MainActivity;
import abdoroid.quranradio.ui.stations.StationsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;
import abdoroid.quranradio.utils.StorageUtils;

public class Categories extends BaseActivity {

    private final ArrayList<CategoriesDataModel> categories = new ArrayList<>();
    private StorageUtils storageUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(Categories.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        Helper.setAnimation(Categories.this);
        setContentView(R.layout.activity_categories);
        categories.add(new CategoriesDataModel(getString(R.string.reciters1_cat), getString(R.string.reciters_des), R.drawable.ic_baseline_radio, "server2/"));
        categories.add(new CategoriesDataModel(getString(R.string.reciters_cat), getString(R.string.reciters_des), R.drawable.ic_baseline_radio, "server1/"));
        categories.add(new CategoriesDataModel(getString(R.string.general_cat), getString(R.string.general_des), R.drawable.ic_baseline_radio, "general/"));
        categories.add(new CategoriesDataModel(getString(R.string.world_cat), getString(R.string.world_des), R.drawable.ic_baseline_radio, "countries/"));
        categories.add(new CategoriesDataModel(getString(R.string.qiraat_cat), getString(R.string.qiraat_des), R.drawable.ic_baseline_radio, "qiraat/"));
        categories.add(new CategoriesDataModel(getString(R.string.translation_cat), getString(R.string.translation_des), R.drawable.ic_baseline_radio, "translation/"));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setClipToPadding(false);
        recyclerView.setHasFixedSize(true);
        CategoriesAdapter adapter = new CategoriesAdapter(this, categories);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        storageUtils = new StorageUtils(getApplicationContext());

        adapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(getApplicationContext(), StationsActivity.class);
            storageUtils.storeQueryString(categories.get(position).getQueryString());
            startActivity(intent);
        });

    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}