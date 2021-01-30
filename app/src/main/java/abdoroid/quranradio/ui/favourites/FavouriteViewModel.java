package abdoroid.quranradio.ui.favourites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.utils.StorageUtils;

public class FavouriteViewModel extends AndroidViewModel {
    public final MutableLiveData<ArrayList<RadioDataModel>> radioStations = new MutableLiveData<>();

    public FavouriteViewModel(@NonNull Application application) {
        super(application);
    }

    public void getFavouriteStations(){
        StorageUtils storageUtils = new StorageUtils(getApplication());
        radioStations.setValue(storageUtils.loadFavourites());
    }
}
