package abdoroid.quranradio.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.util.ArrayList;
import java.util.Locale;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.services.RecordsPlayerService;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;
import abdoroid.quranradio.utils.StorageUtils;

public class RecordsPlayerActivity extends BaseActivity implements View.OnClickListener {

    public static final String LIST_POSITION = "124";
    public static final String AUDIO_LIST = "list";
    private RecordsPlayerService player;
    private boolean serviceBound;
    private ArrayList<RadioDataModel> audioList = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int position;
    public static final String Broadcast_PLAY_NEW_AUDIO = "abdoroid.quranradio.ui.player.PlayNewAudio";
    private TextView timeText, titleText;
    public static BarVisualizer mVisualizer;
    private ImageButton playBtn;
    private ImageButton previousBtn, nextBtn, seekFdBtn, seekBkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Helper.setAnimation(RecordsPlayerActivity.this);
        LocaleHelper.setLocale(RecordsPlayerActivity.this);
        setContentView(R.layout.activity_records_player);

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

        previousBtn = findViewById(R.id.previous);
        nextBtn = findViewById(R.id.next);
        playBtn = findViewById(R.id.play);
        seekFdBtn = findViewById(R.id.seek_frd);
        seekBkBtn = findViewById(R.id.seek_bkd);
        mVisualizer = findViewById(R.id.visualizer);
        titleText = findViewById(R.id.station_title);
        titleText.setText(audioList.get(position).getName());
        playBtn.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        seekFdBtn.setOnClickListener(this);
        seekBkBtn.setOnClickListener(this);

        seekFdBtn.setOnLongClickListener(v -> {
            seekFastForward();
            return true;
        });
        seekBkBtn.setOnLongClickListener(v -> {
            seekFastBackward();
            return true;
        });

    }

    @Override
    public void onClick(View v) {
        if (playBtn.equals(v)) {
            if (isPlaying()) {
                pauseStation();
                playBtn.setImageResource(R.drawable.play);
            } else {
                playStation();
                playBtn.setImageResource(R.drawable.pause);
            }
        } else if (nextBtn.equals(v)) {
            playNext();
        } else if (previousBtn.equals(v)) {
            playPrev();
        } else if (seekFdBtn.equals(v)) {
            seekForward();
        } else if (seekBkBtn.equals(v)) {
            seekBackward();
        }
    }

    private void playNext(){
        player.skipToNext();
        position++;
        if (position > audioList.size()-1){
            position = 0;
        }
    }

    private void playPrev(){
        player.skipToPrevious();
        position--;
        if (position < 0){
            position = audioList.size()-1;
        }
    }

    private void playStation(){
        player.playMedia();
    }

    private void pauseStation(){
        player.pauseMedia();
    }

    private void seekForward(){
        player.seekFd();
        Toast.makeText(getApplicationContext(), "+5 sec", Toast.LENGTH_SHORT).show();
    }

    private void seekFastForward(){
        player.seekFFd();
        Toast.makeText(getApplicationContext(), "+15 sec", Toast.LENGTH_SHORT).show();
    }

    private void seekBackward(){
        player.seekBd();
        Toast.makeText(getApplicationContext(), "-5 sec", Toast.LENGTH_SHORT).show();
    }

    private void seekFastBackward(){
        player.seekFBd();
        Toast.makeText(getApplicationContext(), "-15 sec", Toast.LENGTH_SHORT).show();
    }

    private boolean isPlaying(){
        return player.isPng();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecordsPlayerService.LocalBinder binder = (RecordsPlayerService.LocalBinder) service;
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

            Intent playerIntent = new Intent(this, RecordsPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            StorageUtils storage = new StorageUtils(getApplicationContext());
            storage.storeAudioIndex(position);

            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }
        if (mVisualizer != null) mVisualizer.release();
        handler.removeCallbacks(mRunnable);
    }

    private void updateUi(){
        if (player.isPaused){
            playBtn.setImageResource(R.drawable.play);
        }else {
            playBtn.setImageResource(R.drawable.pause);
        }
        timeText.setText(convertMilliSecToTimeString(player.getMediaPosition()));
        titleText.setText(player.getPlayingNow());
    }

    private String convertMilliSecToTimeString(long time){
        int[] allTimes = Helper.getTimeFromMilliseconds(time);
        Locale locale = Locale.getDefault();
        return (String.format(locale, "%02d:%02d:%02d", allTimes[0],allTimes[1],allTimes[2]));
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            updateUi();
            handler.postDelayed(mRunnable, 100);
        }
    };


}