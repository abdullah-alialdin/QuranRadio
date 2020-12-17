package abdoroid.quranradio.ui.recordings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Map;

import abdoroid.quranradio.pojo.RadioDataModel;

public class RecordsViewModel extends AndroidViewModel {
    public final MutableLiveData<ArrayList<RadioDataModel>> recordings = new MutableLiveData<>();

    public RecordsViewModel(@NonNull Application application) {
        super(application);
    }

    public void getRecordedStations(){
        SharedPreferences sharedPreferences =
                getApplication().getSharedPreferences("RecordPreferences", Context.MODE_PRIVATE);
        Map<String, ?> allData = sharedPreferences.getAll();
        ArrayList<RadioDataModel> recordingsList = new ArrayList<>();
        for (String key : allData.keySet()){
            String value = (String) allData.get(key);
            recordingsList.add(new RadioDataModel(key, value));
        }

        recordings.setValue(recordingsList);
    }
}