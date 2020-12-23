package abdoroid.quranradio.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class RadioDataModel implements Parcelable {


    @SerializedName("name")
    private final String name;
    @SerializedName("radio_url")
    private final String radio_url;

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

    public static final Parcelable.Creator<RadioDataModel> CREATOR = new Parcelable.Creator<RadioDataModel>() {
        public RadioDataModel createFromParcel(Parcel in) {
            return new RadioDataModel(in);
        }

        public RadioDataModel[] newArray(int size) {
            return new RadioDataModel[size];
        }
    };
}
