package abdoroid.quranradio.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import abdoroid.quranradio.pojo.RadioDataModel;

public class Radios {
    @SerializedName("radios")
    private final ArrayList<RadioDataModel> radios;

    @SuppressWarnings("unused")
    public Radios(ArrayList<RadioDataModel> radioChannels){
        this.radios = radioChannels;
    }

    public ArrayList<RadioDataModel> getRadioChannels() {
        return radios;
    }

}
