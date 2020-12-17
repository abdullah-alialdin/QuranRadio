package abdoroid.quranradio.ui.favourites;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Map;

import abdoroid.quranradio.pojo.RadioDataModel;

public class FavouriteViewModel extends AndroidViewModel {
    public final MutableLiveData<ArrayList<RadioDataModel>> radioStations = new MutableLiveData<>();

    public FavouriteViewModel(@NonNull Application application) {
        super(application);
    }

    public void getFavouriteStations(){
        SharedPreferences sharedPreferences =
                getApplication().getSharedPreferences("StationList", Context.MODE_PRIVATE);
        Map<String, ?> allData = sharedPreferences.getAll();
        ArrayList<RadioDataModel> favStations = new ArrayList<>();
        for (String key : allData.keySet()) {
            String value = (String) allData.get(key);
            favStations.add(new RadioDataModel(value, key));
        }

        radioStations.setValue(favStations);
    }
}
