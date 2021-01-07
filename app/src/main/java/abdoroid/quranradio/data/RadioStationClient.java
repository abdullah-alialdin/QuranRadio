package abdoroid.quranradio.data;

import android.util.Log;

import java.util.Locale;

import abdoroid.quranradio.pojo.Radios;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RadioStationClient {
    private static final String BASE_URL = "https://api.mp3quran.net/radios/";
    private String query;
    private final RadioApi radioApi;
    private static RadioStationClient INSTANCE;

    public RadioStationClient(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        radioApi = retrofit.create(RadioApi.class);
    }

    public static RadioStationClient getINSTANCE() {
        if (INSTANCE == null){
            INSTANCE = new RadioStationClient();
        }
        return INSTANCE;
    }

    public Call<Radios> getRadioStations(){
        String localeLang = Locale.getDefault().getDisplayLanguage();
        Log.d("Language", localeLang);
        switch (localeLang) {
            case "العربية":
                query = "radio_arabic.json";
                break;
            case "français":
                query = "radio_french.json";
                break;
            case "English":
                query = "radio_english.json";
                break;
            case "Deutsch":
                query = "radio_german.json";
                break;
            case "español":
                query = "radio_spanish.json";
                break;
            case "Bahasa Indonesia":
                query = "radio_indonesian.json";
                break;
            case "português":
                query = "radio_portuguese.json";
                break;
            case "русский":
                query = "radio_russian.json";
                break;
            case "Kenya":
                query = "radio_swahili.json";
                break;
            case "Türkçe":
                query = "radio_turkish.json";
                break;
            case "中文":
                query = "radio_chinese.json";
                break;
        }
        return radioApi.getRadioStations(query);
    }
}
