package abdoroid.quranradio.pojo;

import java.util.ArrayList;

import abdoroid.quranradio.pojo.RadioDataModel;

public class Radios {
    private final ArrayList<RadioDataModel> radios;

    @SuppressWarnings("unused")
    public Radios(ArrayList<RadioDataModel> radioChannels){
        this.radios = radioChannels;
    }

    public ArrayList<RadioDataModel> getRadioChannels() {
        return radios;
    }

}
