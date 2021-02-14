package abdoroid.quranradio.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import abdoroid.quranradio.pojo.RadioDataModel;

public class StorageUtils {

    private final String STORAGE = "abdoroid.quranradio.utils.STORAGE";
    private final String FAVOURITES_KEY = "abdoroid.quranradio.utils.FAVOURITES";
    private final String RECORDINGS_KEY = "abdoroid.quranradio.utils.RECORDINGS";
    private final String PLAYER_TYPE_KEY = "abdoroid.quranradio.utils.PLAYER";
    private final String LANGUAGE_KEY = "abdoroid.quranradio.utils.LANGUAGE";
    public final String STATION_PLAYER = "STATIONS";
    public final String FAVOURITES_PLAYER = "FAVOURITES";
    public final String RECORDINGS_PLAYER = "RECORDINGS";
    private SharedPreferences preferences;
    private final Context context;

    public StorageUtils(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<RadioDataModel> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<RadioDataModel> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<RadioDataModel>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeFavourite(String url, String title){
        writeToPreferences(FAVOURITES_KEY, url, title);
    }

    public ArrayList<RadioDataModel> loadFavourites() {
        return readFromPreferences(FAVOURITES_KEY);
    }

    public boolean checkFavourites(String url){
        preferences = context.getSharedPreferences(FAVOURITES_KEY, Context.MODE_PRIVATE);
        return (preferences.contains(url));
    }

    public void removeFavourites(String url){
        preferences = context.getSharedPreferences(FAVOURITES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(url);
        editor.apply();
    }

    public void storeRecordings(String url, String title){
        writeToPreferences(RECORDINGS_KEY, url, title);
    }

    public ArrayList<RadioDataModel> loadRecordings() {
        return readFromPreferences(RECORDINGS_KEY);
    }

    public void removeRecordings(String url){
        preferences = context.getSharedPreferences(RECORDINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(url);
        editor.apply();
    }


    private void writeToPreferences(String sharedKey, String url, String title){
        preferences = context.getSharedPreferences(sharedKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(url, title);
        editor.apply();
    }

    private ArrayList<RadioDataModel> readFromPreferences(String sharedkey){
        preferences = context.getSharedPreferences(sharedkey, Context.MODE_PRIVATE);
        Map<String, ?> allData = preferences.getAll();
        ArrayList<RadioDataModel> stations = new ArrayList<>();
        for (String key : allData.keySet()) {
            String value = (String) allData.get(key);
            stations.add(new RadioDataModel(value, key));
        }
        return stations;
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);
    }

    public void setPlayerType(String type){
        preferences = context.getSharedPreferences(PLAYER_TYPE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("playerType", type);
        editor.apply();
    }

    public String getPlayerType(){
        preferences = context.getSharedPreferences(PLAYER_TYPE_KEY, Context.MODE_PRIVATE);
        return preferences.getString("playerType", STATION_PLAYER);
    }

    public void storeLanguage(String language){
        preferences = context.getSharedPreferences(LANGUAGE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("LANGUAGE", language);
        editor.apply();
    }

    public String loadLanguage(){
        preferences = context.getSharedPreferences(LANGUAGE_KEY, Context.MODE_PRIVATE);
        return preferences.getString("LANGUAGE", Locale.getDefault().getLanguage());
    }

    public void storeSelectedTime(long time){
        preferences = context.getSharedPreferences(LANGUAGE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("TIME", time);
        editor.apply();
    }

    public long loadSelectedTime(){
        preferences = context.getSharedPreferences(LANGUAGE_KEY, Context.MODE_PRIVATE);
        return preferences.getLong("TIME", 0);
    }

    public void storeDarkMode(boolean state){
        preferences = context.getSharedPreferences(LANGUAGE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("darkMode", state);
        editor.apply();
    }

    public boolean loadDarkMode(){
        preferences = context.getSharedPreferences(LANGUAGE_KEY, Context.MODE_PRIVATE);
        return preferences.getBoolean("darkMode", true);
    }

    public void storeQueryString(String query){
        preferences = context.getSharedPreferences(LANGUAGE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("query", query);
        editor.apply();
    }

    public String loadQueryString(){
        preferences = context.getSharedPreferences(LANGUAGE_KEY, Context.MODE_PRIVATE);
        return preferences.getString("query", "server2/");
    }
}

