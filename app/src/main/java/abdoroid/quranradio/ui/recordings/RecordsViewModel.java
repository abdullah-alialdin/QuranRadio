package abdoroid.quranradio.ui.recordings;

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

public class RecordsViewModel extends AndroidViewModel {
    public MutableLiveData<ArrayList<RadioDataModel>> recordings = new MutableLiveData<>();

    public RecordsViewModel(@NonNull Application application) {
        super(application);
    }

    public void getRecordedStations(){
        SharedPreferences sharedPreferences =
                getApplication().getSharedPreferences("RecordPreferences", Context.MODE_PRIVATE);
        Map<String, ?> allData = sharedPreferences.getAll();
        ArrayList<RadioDataModel> recordingsList = new ArrayList<>();
        Set set = allData.entrySet();
        Iterator itr = set.iterator();

        while(itr.hasNext()){
            Map.Entry entry = (Map.Entry)itr.next();
            recordingsList.add(new RadioDataModel(entry.getKey().toString(), entry.getValue().toString()));
        }

        recordings.setValue(recordingsList);
    }
}