package abdoroid.quranradio.ui.settings;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import abdoroid.quranradio.R;
import abdoroid.quranradio.ui.main.MainActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;

public class SettingsActivity extends BaseActivity {

    private RadioGroup langGroup;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Button okBtn, cancelBtn;
    private Toolbar toolbar;
    private SwitchCompat switchCompat;
    private EditText timeInputHours, timeInputMinutes;
    private int hours, minutes;
    private Spinner spinner;
    private LinearLayout customTimeView;
    private long streamingTime;
    private boolean reload = false;
    private TextView toolbarTitle;
    private ImageView toolbarImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
        toolbar = findViewById(R.id.my_toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.settings));
        toolbarImage =  findViewById(R.id.toolbar_image);
        toolbarImage.setImageResource(R.drawable.ic_baseline_settings_24);
        preferences = this.getSharedPreferences("Language", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String lang = preferences.getString(LocaleHelper.SELECTED_LANGUAGE, Locale.getDefault().getLanguage());
        langGroup = findViewById(R.id.lang_radio_group);
        langGroup.check(getCheckedId(lang));
        langGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.arabic:
                        selectLanguage("ar");
                        reload = true;
                        break;
                    case R.id.english:
                        selectLanguage("en");
                        reload = true;
                        break;
                    case R.id.french:
                        selectLanguage("fr");
                        reload = true;
                        break;
                }

            }
        });
        boolean isChecked = preferences.getBoolean("darkMode", true);
        switchCompat = findViewById(R.id.dark_mode_switch);
        switchCompat.setChecked(isChecked);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                darkModeIsChecked(b);
                reload = true;
            }
        });

        streamingTime = preferences.getLong("StreamTime", 0);
        int[] timeInHours = Helper.getTimeFromMilliseconds(streamingTime);

        spinner = findViewById(R.id.spinner);
        String[] spinnerChoices = {getString(R.string.forever), getString(R.string.custom)};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerChoices);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinAdapter);
        customTimeView = findViewById(R.id.custom_time);
        if (streamingTime != 0){
            spinner.setSelection(1);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    streamingTime = 0;
                    customTimeView.setVisibility(View.GONE);
                }else {
                    customTimeView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        timeInputHours = findViewById(R.id.hours_edit_text);
        timeInputHours.setText(String.format("%02d", timeInHours[0]));

        timeInputMinutes = findViewById(R.id.minutes_edit_text);
        timeInputMinutes.setText(String.format("%02d", timeInHours[1]));

        okBtn = findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (customTimeView.getVisibility() == View.VISIBLE){
                    if (timeInputHours.getText().toString().length() > 0){
                        hours = Integer.valueOf(timeInputHours.getText().toString());
                    }
                    if (timeInputMinutes.getText().toString().length() > 0){
                        minutes = Integer.valueOf(timeInputMinutes.getText().toString());
                    }
                    streamingTime = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes));
                    setStreamTime(streamingTime);
                }else {
                    setStreamTime(streamingTime);
                }

                if (reload){
                    MainActivity.needReloaded = true;
                }
                finish();
            }
        });
        cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void selectLanguage(String lang){
        editor.putString(LocaleHelper.SELECTED_LANGUAGE, lang);
        editor.apply();
    }

    private void darkModeIsChecked(boolean isChecked){
        editor.putBoolean("darkMode", isChecked);
        editor.apply();
    }

    private int getCheckedId(String lang){
        switch (lang){
            case "ar":
                return R.id.arabic;
            case "fr":
                return R.id.french;
            default:
                return R.id.english;
        }
    }

    private void setStreamTime(long streamTime){
        editor.putLong("StreamTime", streamTime);
        editor.apply();
    }

}