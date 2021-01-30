package abdoroid.quranradio.ui.player;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

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
import abdoroid.quranradio.services.MediaPlaybackService;
import abdoroid.quranradio.ui.favourites.FavouriteActivity;
import abdoroid.quranradio.ui.recordings.RecordsActivity;
import abdoroid.quranradio.ui.stations.StationsActivity;
import abdoroid.quranradio.utils.BaseActivity;
import abdoroid.quranradio.utils.Helper;
import abdoroid.quranradio.utils.LocaleHelper;
import abdoroid.quranradio.utils.StorageUtils;

public class PlayerActivity extends BaseActivity implements View.OnClickListener {

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCallback mediaControllerCallback;
    private MediaControllerCompat controller;
    private ArrayList<RadioDataModel> audioList = new ArrayList<>();
    private int position;
    private TextView timeView, titleText, selectedTimeView;
    private ImageButton favBtn, recordBtn, backwardBtn, forwardBtn, seekForward, seekBackward;
    private ImageView recordAnmi;
    private ImageButton playToggleButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static String fileUrl = null;
    private boolean mStartRecording = true;
    private String fileName, date;
    private String audio_url, audio_title;
    private InputStream inputStream;
    private Thread thread;
    private AnimationDrawable recordAnimation;
    private long selectedStreamTime;
    private StorageUtils storageUtils;
    private AnimationDrawable playerAnimation;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(PlayerActivity.this);
        AppCompatDelegate.setDefaultNightMode(Helper.setDarkMode(this));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_player);
        ImageView playerAnim = findViewById(R.id.player_animation_view);
        playerAnimation = (AnimationDrawable) playerAnim.getBackground();
        playerAnimation.start();
        storageUtils = new StorageUtils(this);
        audioList = storageUtils.loadAudio();
        position = storageUtils.loadAudioIndex();
        timeView = findViewById(R.id.time_view);
        selectedTimeView = findViewById(R.id.selected_time_view);
        initMediaBrowser();
        if (mediaBrowser.isConnected()){
            if (MediaControllerCompat.getMediaController(this) == null){
                registerMediaController(mediaBrowser.getSessionToken());
            }
        }else {
            mediaBrowser.connect();
        }
        selectedStreamTime = storageUtils.loadSelectedTime();
        backwardBtn = findViewById(R.id.previous);
        forwardBtn = findViewById(R.id.next);
        playToggleButton = findViewById(R.id.play);
        seekForward = findViewById(R.id.seek_frd);
        seekBackward = findViewById(R.id.seek_bkd);
        favBtn = findViewById(R.id.fav);
        recordBtn = findViewById(R.id.record);
        recordAnmi = findViewById(R.id.record_anmi_view);
        recordAnmi.setBackgroundResource(R.drawable.record_animation);
        recordAnimation = (AnimationDrawable) recordAnmi.getBackground();
        if (storageUtils.getPlayerType().equals(storageUtils.RECORDINGS_PLAYER)){
            favBtn.setVisibility(View.GONE);
            recordBtn.setVisibility(View.GONE);
            seekBackward.setVisibility(View.VISIBLE);
            seekForward.setVisibility(View.VISIBLE);
        }
        titleText = findViewById(R.id.station_title);
        audio_url = audioList.get(position).getUrl();
        audio_title = audioList.get(position).getName();
        checkFavourite(audio_url);
        titleText.setText(audio_title);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss", Locale.getDefault());
        date = dateFormat.format(c);
        favBtn.setOnClickListener(this);
        playToggleButton.setOnClickListener(this);
        backwardBtn.setOnClickListener(this);
        forwardBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        seekBackward.setOnClickListener(this);
        seekForward.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (playToggleButton.equals(v)) {
            togglePlayPause();
        } else if (forwardBtn.equals(v)) {
            if (controller != null){
                controller.getTransportControls().skipToNext();
            }
        } else if (backwardBtn.equals(v)) {
            if (controller != null){
                controller.getTransportControls().skipToPrevious();
            }
        } else if (recordBtn.equals(v)) {
            fileUrl = getExternalCacheDir().getAbsolutePath();
            fileName = (audioList.get(position).getName() + " " + date);
            fileUrl += fileName + ".mp3";
            onRecord(mStartRecording);
            mStartRecording = !mStartRecording;
        } else if (favBtn.equals(v)) {
            if (!storageUtils.checkFavourites(audio_url)) {
                favBtn.setImageResource(R.drawable.love_ok);
                storageUtils.storeFavourite(audio_url, audio_title);
            } else {
                favBtn.setImageResource(R.drawable.love);
                storageUtils.removeFavourites(audio_url);
            }
        }else if (seekForward.equals(v)){
            seekBy(5);
        }else if (seekBackward.equals(v)){
            seekBy(-5);
        }

    }
    private void togglePlayPause(){
        controller = MediaControllerCompat.getMediaController(PlayerActivity.this);
        if (controller.getPlaybackState() != null){
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING){
                controller.getTransportControls().pause();
            }else {
                controller.getTransportControls().play();
            }
        }else {
            controller.getTransportControls().play();
        }
    }

    private void startPlayerAnimator(boolean animatePlayer){
        if (animatePlayer){
            playerAnimation.start();
        } else {
            playerAnimation.stop();
        }
    }

    private void seekBy(int seconds){
        MediaControllerCompat controller =
                MediaControllerCompat.getMediaController(this);
        long newPosition = controller.getPlaybackState().getPosition() + seconds*1000;
        controller.getTransportControls().seekTo(newPosition);
    }

    private void playAgain(){
        controller = MediaControllerCompat.getMediaController(PlayerActivity.this);
        controller.getTransportControls().play();
        startPlayerAnimator(true);
        playToggleButton.setActivated(true);
        if (controller.getPlaybackState() != null) {
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                updateUiViews(PlaybackStateCompat.STATE_PLAYING);
            }
        }
    }

    private void updateUiViews(int state){
        boolean isPlaying = (state == PlaybackStateCompat.STATE_PLAYING);
        playToggleButton.setActivated(isPlaying);
        startPlayerAnimator(isPlaying);
        handler.post(timeViewSetting);
        titleText.setText(controller.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        position = storageUtils.loadAudioIndex();
        checkFavourite(audioList.get(position).getUrl());
    }

    final Runnable timeViewSetting = new Runnable() {
        @Override
        public void run() {
            long time = controller.getPlaybackState().getPosition();
            if (selectedStreamTime != 0) {
                if (DateUtils.formatElapsedTime(time/1000)
                        .equals(DateUtils.formatElapsedTime(selectedStreamTime/1000))) {
                    controller.getTransportControls().stop();
                    selectedTimeView.setVisibility(View.VISIBLE);
                    handler.removeCallbacks(timeViewSetting);
                }
            }
            timeView.setText(DateUtils.formatElapsedTime(time/1000));
            handler.post(timeViewSetting);
        }
    };

    private void registerMediaController(MediaSessionCompat.Token token){
        MediaControllerCompat mediaController = new MediaControllerCompat(PlayerActivity.this, token);
        MediaControllerCompat.setMediaController(PlayerActivity.this, mediaController);
        mediaControllerCallback = new MediaControllerCallback();
        mediaController.registerCallback(mediaControllerCallback);
    }

    private void initMediaBrowser(){
        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MediaPlaybackService.class),
                new MediaBrowserCallbacks(), null);
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
        storageUtils.storeRecordings(fileUrl, fileName);
    }

    private void checkFavourite(String url){
        if (storageUtils.checkFavourites(url)){
            favBtn.setImageResource(R.drawable.love_ok);
        }else{
            favBtn.setImageResource(R.drawable.love);
        }
    }

    class MediaControllerCallback extends MediaControllerCompat.Callback{
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            updateUiViews(state.getState());
            if (!mStartRecording){
                stopRecording();
                mStartRecording = !mStartRecording;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

        }
    }

    class MediaBrowserCallbacks extends MediaBrowserCompat.ConnectionCallback{
        @Override
        public void onConnected() {
            super.onConnected();
            registerMediaController(mediaBrowser.getSessionToken());
            playAgain();
        }

        @SuppressWarnings("EmptyMethod")
        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
        }

        @SuppressWarnings("EmptyMethod")
        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MediaControllerCompat.getMediaController(this) != null){
            if (mediaControllerCallback != null){
                MediaControllerCompat.getMediaController(this).unregisterCallback(mediaControllerCallback);
            }
        }
        if (!mStartRecording){
            stopRecording();
        }
        handler.removeCallbacks(timeViewSetting);
    }

    @Override
    public void onBackPressed() {
        if (storageUtils.getPlayerType().equals(storageUtils.FAVOURITES_PLAYER)){
            startActivity(new Intent(this, FavouriteActivity.class));
        }else if (storageUtils.getPlayerType().equals(storageUtils.RECORDINGS_PLAYER)){
            startActivity(new Intent(this, RecordsActivity.class));
        }else {
            startActivity(new Intent(this, StationsActivity.class));
        }
        if (!mStartRecording){
            stopRecording();
        }
        finish();
    }

 }