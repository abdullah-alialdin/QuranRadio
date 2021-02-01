package abdoroid.quranradio.services;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.utils.StorageUtils;

public class MediaPlayerCallback extends MediaSessionCompat.Callback
        implements AudioManager.OnAudioFocusChangeListener {

    private final Context context;
    private final MediaSessionCompat mediaSession;
    private MediaPlayer mediaPlayer;
    PlayerListener listener;
    private int stationsListPosition;
    private ArrayList<RadioDataModel> stations = new ArrayList<>();
    private StorageUtils storageUtils;
    private AudioFocusRequest focusRequest;
    private AudioManager audioManager;
    private boolean paused = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    MediaPlayerCallback(Context context, MediaSessionCompat mediaSession) {
        this.context = context;
        this.mediaSession = mediaSession;
    }

    private void setAudioList(){
        storageUtils = new StorageUtils(context);
        stations = storageUtils.loadAudio();
        stationsListPosition = storageUtils.loadAudioIndex();
    }

    private String getAudioUrl(){
        setAudioList();
        return stations.get(stationsListPosition).getUrl();
    }


    private String getAudioTitle(){
        setAudioList();
        return stations.get(stationsListPosition).getName();
    }

    private void setState(int state){
        long position = -1;
        if (mediaPlayer != null){
            position = mediaPlayer.getCurrentPosition();
        }
        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_STOP |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_FAST_FORWARD |
                        PlaybackStateCompat.ACTION_REWIND |
                        PlaybackStateCompat.ACTION_PAUSE).setState(state, position, 1.0f)
                .build();
        mediaSession.setPlaybackState(playbackState);
        if (state == PlaybackStateCompat.STATE_PAUSED ||
                state == PlaybackStateCompat.STATE_PLAYING) {
            listener.onStateChange();
        }
    }

    private void initializeMediaPlayer(){
        if (mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(mp -> setState(PlaybackStateCompat.STATE_STOPPED));
        }
    }

    private void prepareMedia(){
        if (mediaSession.getController().getMetadata() != null &&
                getAudioUrl().equals(mediaSession.getController().getMetadata()
                        .getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))){
            if (mediaSession.getController().getPlaybackState() != null &&
                    mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED){
                prepare();
            }else {
                startPlaying();
            }
        } else {
            prepare();
        }
    }

    private void prepare(){
        if (mediaPlayer != null){
            mediaPlayer.setOnPreparedListener(mp -> startPlaying());
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(context, Uri.parse(getAudioUrl()));
            }catch (IOException e){
                e.printStackTrace();
            }
            try{
                mediaPlayer.prepareAsync();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                            getAudioTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, getAudioUrl())
                    .build());
        }
    }

    private void startPlaying(){
        if (mediaPlayer != null){
            if (!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                setState(PlaybackStateCompat.STATE_PLAYING);
                if (storageUtils.loadSelectedTime() != 0 && !storageUtils.getPlayerType().equals(storageUtils.RECORDINGS_PLAYER)) {
                    handler.postDelayed(runnable, storageUtils.loadSelectedTime());
                }
            }
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            onStop();
            Toast.makeText(context, context.getString(R.string.change_time), Toast.LENGTH_LONG).show();
            handler.removeCallbacks(runnable);
        }
    };

    private void pausePlaying() {
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                setState(PlaybackStateCompat.STATE_PAUSED);
            }
        }
        listener.onPausePlaying();
    }

    private void stopPlaying() {
        mediaSession.setActive(false);
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                setState(PlaybackStateCompat.STATE_STOPPED);
            }
        }
        listener.onStopPlaying();
    }

    @Override
    public void onPlay() {
        requestAudioFocus();
        mediaSession.setActive(true);
        initializeMediaPlayer();
        prepareMedia();
    }

    @Override
    public void onPause() {
        pausePlaying();
    }

    @Override
    public void onStop() {
        removeAudioFocus();
        stopPlaying();
    }

    @Override
    public void onSkipToNext() {
        skipToNext();
    }

    private void skipToNext(){
        if (stationsListPosition == stations.size() - 1) {
            stationsListPosition = 0;
        } else {
            stationsListPosition++;
        }
        storageUtils.storeAudioIndex(stationsListPosition);
        onPlay();
    }

    @Override
    public void onSkipToPrevious() {
        skipToPrevious();
    }

    private void skipToPrevious(){
        if (stationsListPosition == 0) {
            stationsListPosition = stations.size() - 1;
        } else {
            stationsListPosition--;
        }
        storageUtils.storeAudioIndex(stationsListPosition);
        onPlay();
    }

    @Override
    public void onSeekTo(long pos) {
        if (mediaPlayer != null){
            mediaPlayer.seekTo((int)pos);
            PlaybackStateCompat playbackState = mediaSession.getController().getPlaybackState();
            if (playbackState != null){
                setState(playbackState.getState());
            }else {
                setState(PlaybackStateCompat.STATE_PAUSED);
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (paused) {
                    onPlay();
                    paused = false;
                }
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.setVolume(1f, 1f);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()){
                    onStop();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()){
                    paused = true;
                    onPause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
        }
    }

    @SuppressWarnings("deprecation")
    private void requestAudioFocus() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build())
                    .build();
            audioManager.requestAudioFocus(focusRequest);
        }
    }

    @SuppressWarnings("deprecation")
    private void removeAudioFocus() {
        audioManager.abandonAudioFocus(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
    }

    interface PlayerListener{
        void onStateChange();
        void onStopPlaying();
        void onPausePlaying();
    }

}

