package abdoroid.quranradio.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.services.RecordsPlayerService;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;

public class RecordsPlayerActivity extends BaseActivity implements View.OnClickListener {

    public static final String LIST_POSITION = "124";
    public static final String AUDIO_LIST = "list";
    private static final int PERM_REQ_CODE = 23;
    private RecordsPlayerService player;
    private boolean serviceBound;
    private ArrayList<RadioDataModel> audioList = new ArrayList<>();
    private int position;
    public static final String Broadcast_PLAY_NEW_AUDIO = "abdoroid.quranradio.ui.player.PlayNewAudio";
    public static TextView timeText, titleText;
    public static BarVisualizer mVisualizer;
    public static ImageButton playBtn;
    private String audio_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

        ImageButton previousBtn = findViewById(R.id.previous);
        ImageButton nextBtn = findViewById(R.id.next);
        playBtn = findViewById(R.id.play);
        ImageButton seekFdBtn = findViewById(R.id.seek_frd);
        ImageButton seekBkBtn = findViewById(R.id.seek_bkd);
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
            case R.id.seek_frd:
                seekForward();
                break;
            case R.id.seek_bkd:
                seekBackward();
                break;
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void playAudio() {
        Log.d("Abdullah", "serviceBound " + serviceBound);
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, RecordsPlayerService.class);
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
    }


}