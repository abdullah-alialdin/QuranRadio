package abdoroid.quranradio.ui.stations;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import abdoroid.quranradio.data.RadioStationClient;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.pojo.Radios;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RadioStationsViewModel extends ViewModel {
    public MutableLiveData<ArrayList<RadioDataModel>> radioStations = new MutableLiveData<>();

    public void getRadioStations(){
        RadioStationClient.getINSTANCE().getRadioStations().enqueue(new Callback<Radios>() {
            @Override
            public void onResponse(Call<Radios> call, Response<Radios> response) {
                radioStations.setValue(response.body().getRadioChannels());
            }

            @Override
            public void onFailure(Call<Radios> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
