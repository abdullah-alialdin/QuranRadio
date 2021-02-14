package abdoroid.quranradio.data;

import android.util.Log;

import java.util.Locale;

import abdoroid.quranradio.pojo.Radios;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RadioStationClient {
    private static final String BASE_URL = "https://abdullah-alialdin.github.io/";
    private String query;
    private final RadioApi radioApi;
    private static RadioStationClient INSTANCE;
    private static String newQuery = "";

    public RadioStationClient(String query){
        String url = BASE_URL + query;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        radioApi = retrofit.create(RadioApi.class);
    }

    public static RadioStationClient getINSTANCE(String query) {
        if (!newQuery.equals(query)){
            INSTANCE = new RadioStationClient(query);
            newQuery = query;
        }
        return INSTANCE;
    }

    public Call<Radios> getRadioStations(){
        String localeLang = Locale.getDefault().getDisplayLanguage();
        Log.d("abdullah", localeLang);
        switch (localeLang) {
            case "العربية":
                query = "arabic.json";
                break;
            case "français":
                query = "french.json";
                break;
            case "English":
                query = "english.json";
                break;
            case "Deutsch":
                query = "german.json";
                break;
            case "español":
                query = "spanish.json";
                break;
            case "Bahasa Indonesia":
            case "Indonesia":
                query = "indonesian.json";
                break;
            case "português":
                query = "portuguese.json";
                break;
            case "русский":
                query = "russian.json";
                break;
            case "Kiswahili":
                query = "swahili.json";
                break;
            case "Türkçe":
                query = "turkish.json";
                break;
            case "中文":
                query = "chinese.json";
                break;
        }
        return radioApi.getRadioStations(query);
    }
}
