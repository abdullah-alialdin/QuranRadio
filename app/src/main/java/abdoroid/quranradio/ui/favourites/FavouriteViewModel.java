package abdoroid.quranradio.ui.favourites;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import abdoroid.quranradio.pojo.RadioDataModel;

public class FavouriteViewModel extends AndroidViewModel {
    public MutableLiveData<ArrayList<RadioDataModel>> radioStations = new MutableLiveData<>();

    public FavouriteViewModel(@NonNull Application application) {
        super(application);
    }

    public void getFavouriteStations(){
        SharedPreferences sharedPreferences =
                getApplication().getSharedPreferences("Abdullah", Context.MODE_PRIVATE);
        Map<String, ?> allData = sharedPreferences.getAll();
        ArrayList<RadioDataModel> favStations = new ArrayList<>();
        Set set = allData.entrySet();
        Iterator itr = set.iterator();

        while(itr.hasNext()){
            Map.Entry entry = (Map.Entry)itr.next();
            favStations.add(new RadioDataModel(entry.getValue().toString(), entry.getKey().toString()));
        }

        radioStations.setValue(favStations);
    }
}
