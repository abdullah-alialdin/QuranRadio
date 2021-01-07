package abdoroid.quranradio.ui.settings;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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

    private SharedPreferences.Editor editor;
    private EditText timeInputHours, timeInputMinutes;
    private int hours, minutes;
    private LinearLayout customTimeView;
    private long streamingTime;
    private boolean reload = false;
    private boolean isChecked;
    private RadioButton arabic, english, french;
    private String lang;


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(SettingsActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.settings));
        ImageView toolbarImage = findViewById(R.id.toolbar_image);
        toolbarImage.setImageResource(R.drawable.ic_baseline_settings);
        SharedPreferences preferences = this.getSharedPreferences("Language", Context.MODE_PRIVATE);
        editor = preferences.edit();
        lang = preferences.getString(LocaleHelper.SELECTED_LANGUAGE, Locale.getDefault().getLanguage());
        Spinner languageSpinner = findViewById(R.id.language_spinner);
        String[] languageSpinnerChoices = {getString(R.string.arabic), getString(R.string.english),
                getString(R.string.german), getString(R.string.spanish),
                getString(R.string.french), getString(R.string.indonesian),
                getString(R.string.portuguese), getString(R.string.russian),
                getString(R.string.swahili), getString(R.string.turkish),
                getString(R.string.chinese)};
        ArrayAdapter<String> langSpinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageSpinnerChoices);
        langSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(langSpinAdapter);
        languageSpinner.setSelection(getCheckedId(lang));
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        selectLanguage("ar");
                        reload = true;
                        break;
                    case 1:
                        selectLanguage("en");
                        reload = true;
                        break;
                    case 2:
                        selectLanguage("de");
                        reload = true;
                        break;
                    case 3:
                        selectLanguage("es");
                        reload = true;
                        break;
                    case 4:
                        selectLanguage("fr");
                        reload = true;
                        break;
                    case 5:
                        selectLanguage("in");
                        reload = true;
                        break;
                    case 6:
                        selectLanguage("pt");
                        reload = true;
                        break;
                    case 7:
                        selectLanguage("ru");
                        reload = true;
                        break;
                    case 8:
                        selectLanguage("sw");
                        reload = true;
                        break;
                    case 9:
                        selectLanguage("tr");
                        reload = true;
                        break;
                    case 10:
                        selectLanguage("zh");
                        reload = true;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        RadioGroup langGroup = findViewById(R.id.lang_radio_group);
//        arabic = findViewById(R.id.arabic);
//        english = findViewById(R.id.english);
//        french = findViewById(R.id.french);
//        langGroup.check(getCheckedId(lang));
//        langGroup.setOnCheckedChangeListener((radioGroup, i) -> {
//            View view = findViewById(i);
//            if (arabic.equals(view)) {
//                selectLanguage("ar");
//                reload = true;
//            } else if (english.equals(view)) {
//                selectLanguage("en");
//                reload = true;
//            } else if (french.equals(view)) {
//                selectLanguage("fr");
//                reload = true;
//            }
//
//        });
        isChecked = preferences.getBoolean("darkMode", true);
        SwitchCompat switchCompat = findViewById(R.id.dark_mode_switch);
        switchCompat.setChecked(isChecked);
        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            isChecked = b;
            reload = true;
        });

        streamingTime = preferences.getLong("StreamTime", 0);
        int[] timeInHours = Helper.getTimeFromMilliseconds(streamingTime);

        Spinner spinner = findViewById(R.id.spinner);
        String[] spinnerChoices = {getString(R.string.forever), getString(R.string.custom)};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerChoices);
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

        Locale locale = Locale.getDefault();
        timeInputHours = findViewById(R.id.hours_edit_text);
        timeInputHours.setText(String.format(locale, "%02d", timeInHours[0]));

        timeInputMinutes = findViewById(R.id.minutes_edit_text);
        timeInputMinutes.setText(String.format(locale,"%02d", timeInHours[1]));

        Button okBtn = findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(view -> {

            darkModeIsChecked(isChecked);
            if (customTimeView.getVisibility() == View.VISIBLE){
                if (timeInputHours.getText().toString().length() > 0){
                    hours = Integer.parseInt(timeInputHours.getText().toString());
                }
                if (timeInputMinutes.getText().toString().length() > 0){
                    minutes = Integer.parseInt(timeInputMinutes.getText().toString());
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
        });
        Button cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(view -> {
            if (reload){
                reload = false;
            }
            selectLanguage(lang);
            finish();
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
                return 0;
            case "zh":
                return 10;
            case "de":
                return 2;
            case "es":
                return 3;
            case "fr":
                return 4;
            case "in":
                return 5;
            case "pt":
                return 6;
            case "ru":
                return 7;
            case "sw":
                return 8;
            case "tr":
                return 9;
            default:
                return 1;
        }
    }

    private void setStreamTime(long streamTime){
        editor.putLong("StreamTime", streamTime);
        editor.apply();
    }

}