package abdoroid.quranradio.ui.player;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

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

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.services.MediaPlayerService;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;

public class PlayerActivity extends BaseActivity implements View.OnClickListener {

    public static final String LIST_POSITION = "124";
    public static final String AUDIO_LIST = "list";
    private static final int PERM_REQ_CODE = 23;
    private MediaPlayerService player;
    private boolean serviceBound;
    private ArrayList<RadioDataModel> audioList = new ArrayList<>();
    private int position;
    public static final String Broadcast_PLAY_NEW_AUDIO = "abdoroid.quranradio.ui.player.PlayNewAudio";
    public static TextView timeText, titleText;
    public static BarVisualizer mVisualizer;
    private ImageButton favBtn, recordBtn;
    public static ImageButton playBtn;
    private SharedPreferences sharedPreferences;
    private static String fileUrl = null;
    private boolean mStartRecording = true;
    private String fileName, date;
    private String audio_url, audio_title;
    private InputStream inputStream;
    private Thread thread;
    private SharedPreferences.Editor recordEditor;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LocaleHelper.setLocale(this);
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
        sharedPreferences = this.getSharedPreferences("Abdullah", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ImageButton previousBtn = findViewById(R.id.previous);
        ImageButton nextBtn = findViewById(R.id.next);
        playBtn = findViewById(R.id.play);
        favBtn = findViewById(R.id.fav);
        recordBtn = findViewById(R.id.record);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
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
        int id = v.getId();
        switch (id){
            case R.id.play:
                if (isPlaying()){
                    pauseStation();
                    playBtn.setImageResource(R.drawable.play);
                }else {
                    playStation();
                    playBtn.setImageResource(R.drawable.pause);
                }
                break;
            case R.id.next:
                playNext();
                audio_title = audioList.get(position).getName();
                break;
            case R.id.previous:
                playPrev();
                audio_title = audioList.get(position).getName();
                break;
            case R.id.record:
                fileUrl = getExternalCacheDir().getAbsolutePath();
                fileName = (audioList.get(position).getName() + " " + date);
                fileUrl += fileName + ".mp3" ;
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
                break;
            case R.id.fav:
                if (!sharedPreferences.contains(audio_url)){
                    favBtn.setImageResource(R.drawable.love_ok);
                    editor.putString(audio_url, audio_title);
                    editor.apply();
                }else{
                    favBtn.setImageResource(R.drawable.love);
                    editor.remove(audio_url);
                    editor.apply();
                }
                break;
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void playAudio() {
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("mediaList", audioList);
            playerIntent.putExtra("position", position);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            broadcastIntent.putExtra("position", position);
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
        thread = new Thread(runnable);
        thread.start();
    }

    Runnable runnable = new Runnable() {
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
    }

    @Override
    public void onBackPressed() {
        Helper.reloadActivity = true;
        super.onBackPressed();
    }
}