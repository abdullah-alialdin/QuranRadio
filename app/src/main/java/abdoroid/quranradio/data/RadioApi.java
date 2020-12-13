package abdoroid.quranradio.data;

import abdoroid.quranradio.pojo.Radios;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RadioApi {

    @GET("{query}")
    Call<Radios> getRadioStations(@Path("query") String query);
}
