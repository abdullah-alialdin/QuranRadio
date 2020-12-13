package abdoroid.quranradio.pojo;

import java.util.ArrayList;
import java.util.List;

import abdoroid.quranradio.pojo.RadioDataModel;

public class Radios {
    private ArrayList<RadioDataModel> radios;

    public Radios(ArrayList<RadioDataModel> radioChannels){
        this.radios = radioChannels;
    }

    public ArrayList<RadioDataModel> getRadioChannels() {
        return radios;
    }

}
