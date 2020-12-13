package abdoroid.quranradio.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RadioGroup;

public class RadioDataModel implements Parcelable {

    private String name;
    private String radio_url;

    public RadioDataModel(String name, String url) {
        this.name = name;
        this.radio_url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return radio_url;
    }

    public RadioDataModel(Parcel in){
        this.name = in.readString();
        this.radio_url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(radio_url);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public RadioDataModel createFromParcel(Parcel in) {
            return new RadioDataModel(in);
        }

        public RadioDataModel[] newArray(int size) {
            return new RadioDataModel[size];
        }
    };
}
