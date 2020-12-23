package abdoroid.quranradio.ui.player;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.services.MediaPlayerService;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;
import abdoroid.quranradio.utils.StorageUtils;

public class PlayerActivity extends BaseActivity implements View.OnClickListener {

    public static final String LIST_POSITION = "124";
    public static final String AUDIO_LIST = "list";
    private static final int PERM_REQ_CODE = 23;
    private MediaPlayerService player;
    private boolean serviceBound;
    private ArrayList<RadioDataModel> audioList = new ArrayList<>();
    private int position;
    public static final String Broadcast_PLAY_NEW_AUDIO = "abdoroid.quranradio.ui.player.PlayNewAudio";
    private TextView timeText, titleText;
    public static CircleLineVisualizer mVisualizer;
    private ImageButton favBtn, recordBtn, previousBtn, nextBtn;
    private ImageView recordAnmi;
    private ImageButton playBtn;
    private SharedPreferences sharedPreferences;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static String fileUrl = null;
    private boolean mStartRecording = true;
    private String fileName, date;
    private String audio_url, audio_title;
    private InputStream inputStream;
    private Thread thread;
    private SharedPreferences.Editor recordEditor;
    private SharedPreferences.Editor editor;
    private AnimationDrawable recordAnimation;
    private long selectedStreamTime;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(PlayerActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_player);
        Intent intent = getIntent();
        audioList = intent.getParcelableArrayListExtra(AUDIO_LIST);
        position = intent.getIntExtra(LIST_POSITION, 0);
        timeText = findViewById(R.id.time_view);
        if (savedInstanceState != null){
            serviceBound = savedInstanceState.getBoolean("ServiceState");
        }else {
            serviceBound = false;
        }
        playAudio();
        sharedPreferences = this.getSharedPreferences("StationList", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Language", Context.MODE_PRIVATE);
        selectedStreamTime = preferences.getLong("StreamTime", 0);
        previousBtn = findViewById(R.id.previous);
        nextBtn = findViewById(R.id.next);
        playBtn = findViewById(R.id.play);
        favBtn = findViewById(R.id.fav);
        recordBtn = findViewById(R.id.record);
        recordAnmi = findViewById(R.id.record_anmi_view);
        recordAnmi.setBackgroundResource(R.drawable.record_animation);
        recordAnimation = (AnimationDrawable) recordAnmi.getBackground();
        mVisualizer = findViewById(R.id.visualizer);
        if (!checkAudioPermission()){
            requestAudioPermission();
        }
        titleText = findViewById(R.id.station_title);
        titleText.setText(audioList.get(position).getName());

        audio_url = audioList.get(position).getUrl();
        audio_title = audioList.get(position).getName();
        checkFavourite(audio_url);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss", Locale.getDefault());
        date = dateFormat.format(c);
        SharedPreferences recordPreferences = this.getSharedPreferences("RecordPreferences", Context.MODE_PRIVATE);
        recordEditor = recordPreferences.edit();

        favBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (playBtn.equals(v)) {
            if (isPlaying()) {
                pauseStation();
            } else {
                playStation();
            }
        } else if (nextBtn.equals(v)) {
            playNext();
            audio_title = audioList.get(position).getName();
        } else if (previousBtn.equals(v)) {
            playPrev();
            audio_title = audioList.get(position).getName();
        } else if (recordBtn.equals(v)) {
            fileUrl = getExternalCacheDir().getAbsolutePath();
            fileName = (audioList.get(position).getName() + " " + date);
            fileUrl += fileName + ".mp3";
            onRecord(mStartRecording);
            mStartRecording = !mStartRecording;
        } else if (favBtn.equals(v)) {
            if (!sharedPreferences.contains(audio_url)) {
                favBtn.setImageResource(R.drawable.love_ok);
                editor.putString(audio_url, audio_title);
                editor.apply();
            } else {
                favBtn.setImageResource(R.drawable.love);
                editor.remove(audio_url);
                editor.apply();
            }
        }

    }

    private void playNext(){
        player.skipToNext();
        position++;
        if (position > audioList.size()-1){
            position = 0;
        }
        if (!mStartRecording){
            stopRecording();
        }
        audio_url = audioList.get(position).getUrl();
        checkFavourite(audio_url);
    }

    private void playPrev(){
        player.skipToPrevious();
        position--;
        if (position < 0){
            position = audioList.size()-1;
        }
        if (!mStartRecording){
            stopRecording();
        }
        audio_url = audioList.get(position).getUrl();
        checkFavourite(audio_url);
    }

    private void playStation(){
       player.playMedia();
    }

    private void pauseStation(){
        player.pauseMedia();
        if (!mStartRecording){
            stopRecording();
        }
    }

    private boolean isPlaying(){
        return player.isPng();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
            handler.postDelayed(mRunnable, 100);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void playAudio() {
        if (!serviceBound) {
            StorageUtils storageUtils = new StorageUtils(getApplicationContext());
            storageUtils.storeAudio(audioList);
            storageUtils.storeAudioIndex(position);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            StorageUtils storage = new StorageUtils(getApplicationContext());
            storage.storeAudioIndex(position);

            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    private void onRecord(boolean start){
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording(){
        recordBtn.setImageResource(R.drawable.stop_record);
        recordAnmi.setVisibility(View.VISIBLE);
        recordAnimation.start();
        thread = new Thread(runnable);
        thread.start();
    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            URL url = null;
            try {
                url = new URL(audioList.get(position).getUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                assert url != null;
                inputStream = url.openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(fileUrl);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int c;

            try {
                while ((c = inputStream.read()) != -1) {
                    assert fileOutputStream != null;
                    fileOutputStream.write(c);
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    };

    private void stopRecording(){
        thread.interrupt();
        recordBtn.setImageResource(R.drawable.record);
        recordAnmi.setVisibility(View.INVISIBLE);
        recordAnimation.stop();
        recordEditor.putString(fileName, fileUrl);
        recordEditor.apply();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    public boolean checkAudioPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.RECORD_AUDIO}, PERM_REQ_CODE);
    }

    private void checkFavourite(String url){
        if (sharedPreferences.contains(url)){
            favBtn.setImageResource(R.drawable.love_ok);
        }else{
            favBtn.setImageResource(R.drawable.love);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }
        if (mVisualizer != null)
            mVisualizer.release();
        if (!mStartRecording){
            stopRecording();
        }
        handler.removeCallbacks(mRunnable);
    }

    @Override
    public void onBackPressed() {
        Helper.reloadActivity = true;
        super.onBackPressed();
    }

    private String convertMilliSecToTimeString(long time) {
        int[] allTimes = Helper.getTimeFromMilliseconds(time);
        Locale locale = Locale.getDefault();
        return (String.format(locale, "%02d:%02d:%02d", allTimes[0], allTimes[1], allTimes[2]));
    }

    private void updateUi(){
        long liveStreamTime = player.getMediaPosition();
        timeText.setText(convertMilliSecToTimeString(liveStreamTime));
        if (selectedStreamTime != 0) {
            if (convertMilliSecToTimeString(liveStreamTime)
                    .equals(convertMilliSecToTimeString(selectedStreamTime))) {
                pauseStation();
                liveStreamTime += 4000;
            }
        }
        if (player.isPaused){
            playBtn.setImageResource(R.drawable.play);
        }else {
            playBtn.setImageResource(R.drawable.pause);
        }
        titleText.setText(player.getPlayingNow());
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            updateUi();
            handler.postDelayed(mRunnable, 100);
        }
    };
}