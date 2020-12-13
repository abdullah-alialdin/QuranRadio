package abdoroid.quranradio.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.utils.PlaybackStatus;
import abdoroid.quranradio.ui.player.RecordsPlayerActivity;
import abdoroid.quranradio.utils.Helper;

public class RecordsPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "abdoroid.quranradio.ui.player.ACTION_PLAY";
    public static final String ACTION_PAUSE = "abdoroid.quranradio.ui.player.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "abdoroid.quranradio.ui.player.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "abdoroid.quranradio.ui.player.ACTION_NEXT";
    public static final String ACTION_STOP = "abdoroid.quranradio.ui.player.ACTION_STOP";


    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private ArrayList<RadioDataModel> audioList = new ArrayList<>();
    private int audioIndex = -1;
    private boolean ongoingCall = false;
    private RadioDataModel activeAudio;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private static final int NOTIFICATION_ID = 101;
    private final String CHANNEL_ID = "Notification Channel Id";
    private boolean isPlaying, isPaused;
    final static String SENDMESAGGE = "passMessage";
    private long selectedStreamTime;
    private final Handler myHandler = new Handler();
    private AudioFocusRequest focusRequest;
    private AudioAttributes audioAttributes;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class LocalBinder extends Binder {
        public RecordsPlayerService getService() {
            return RecordsPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        callStateListener();
        registerBecomingNoisyReceiver();
        registerPlayNewAudio();
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("Language", Context.MODE_PRIVATE);
        selectedStreamTime = preferences.getLong("StreamTime", 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            audioList = intent.getExtras().getParcelableArrayList("mediaList");
            audioIndex = intent.getExtras().getInt("position");
        }catch (NullPointerException e){
            stopSelf();
        }
        activeAudio = audioList.get(audioIndex);
        if (!requestAudioFocus()){
            stopSelf();
        }
        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();

        mediaPlayer.setAudioAttributes(audioAttributes);
        try {
            mediaPlayer.setDataSource(this, Uri.parse(activeAudio.getUrl()));
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.prepareAsync();
    }

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            buildNotification(PlaybackStatus.PLAYING);
            myHandler.postDelayed(UpdateSongTime, 1000);
        }
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            buildNotification(PlaybackStatus.PAUSED);
            isPaused = true;
        }
    }

    public void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            myHandler.removeCallbacks(UpdateSongTime);
        }
    }

    public void seekFd(){
        if (mediaPlayer != null && isPng()){
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
        }
    }

    public void seekFFd(){
        if (mediaPlayer != null && isPng()){
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 15000);
        }
    }

    public void seekFBd(){
        if (mediaPlayer != null && isPng()){
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 15000);
        }
    }

    public void seekBd(){
        if (mediaPlayer != null && isPng()){
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
        }
    }

    public boolean isPng(){
        return mediaPlayer.isPlaying();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
        RecordsPlayerActivity.playBtn.setImageResource(R.drawable.play);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
        if ( ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED){
            RecordsPlayerActivity.mVisualizer.setAudioSessionId(getSessionId());
        }
    }


    public String getPlayingNow(){
        return activeAudio.getName();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d("Abdullah", "gain" + isPaused);
                if (!isPaused){
                    if (mediaPlayer == null) {
                        initMediaPlayer();
                    } else if (!isPng()){
                        mediaPlayer.start();
                    }
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d("Abdullah", "loss" + isPaused);
                if (isPng()) {
                    mediaPlayer.stop();
                }
                myHandler.removeCallbacks(UpdateSongTime);
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d("Abdullah", "loss trans" + isPaused);
                if (isPng()) {
                    mediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d("Abdullah", "can duck" + isPaused);
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @SuppressWarnings( "deprecation" )
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(audioAttributes)
                    .build();
            result = audioManager.requestAudioFocus(focusRequest);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @SuppressWarnings( "deprecation" )
    private void removeAudioFocus() {
        audioManager.abandonAudioFocus(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
    }

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    };

    private void registerBecomingNoisyReceiver(){
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void callStateListener(){
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                playMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private final BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioIndex = intent.getExtras().getInt("position");
            if (audioIndex != -1 && audioIndex < audioList.size()){
                activeAudio = audioList.get(audioIndex);
            }else {
                stopSelf();
            }
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void registerPlayNewAudio(){
        IntentFilter intentFilter = new IntentFilter(RecordsPlayerActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, intentFilter);
    }

    private void initMediaSession() throws RemoteException{
        if (mediaSessionManager != null) return;
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(this, "MediaPlayerService");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        updateMetaData();
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                playMedia();
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                stopMedia();
                removeNotification();
            }
        });

    }

    private void updateMetaData(){
        mediaSession.setMetadata(new MediaMetadataCompat.Builder().
                putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getName()).build());
    }

    public void skipToNext(){
        if (audioIndex == audioList.size() - 1) {
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(++audioIndex);
        }
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
        RecordsPlayerActivity.titleText.setText(getPlayingNow());
    }

    public void skipToPrevious(){
        if (audioIndex == 0) {
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(--audioIndex);
        }
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
        RecordsPlayerActivity.titleText.setText(getPlayingNow());
    }

    public int getSessionId(){
        if (mediaPlayer.isPlaying()){
            return mediaPlayer.getAudioSessionId();
        }
        return 0;
    }

    private void buildNotification(PlaybackStatus playbackStatus){
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;
        if (playbackStatus == PlaybackStatus.PLAYING) {
            play_pauseAction = playbackAction(1);
            RecordsPlayerActivity.playBtn.setImageResource(R.drawable.pause);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            RecordsPlayerActivity.playBtn.setImageResource(R.drawable.play);
            play_pauseAction = playbackAction(0);
        }

        createNotificationChannel();
        Intent intent = new Intent(getApplicationContext(), RecordsPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.largicon);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2))
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.smallicon)
                .setContentText(activeAudio.getName())
                .setContentIntent(pendingIntent)
                .setLargeIcon(largeIcon)
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Media playback";
            String des = "Media playback controls";
            int importance = NotificationManagerCompat.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationChannel.setDescription(des);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void removeNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber){
        Intent playbackAction = new Intent(this, RecordsPlayerService.class);
        switch (actionNumber){
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction){
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private String convertMillisecToTimeString(long time){
        int[] allTimes = Helper.getTimeFromMilliseconds(time);
        return (String.format("%02d:%02d:%02d", allTimes[0],allTimes[1],allTimes[2]));
    }

    private final Runnable UpdateSongTime = new Runnable() {
        public void run() {
            long liveStreamTime = mediaPlayer.getCurrentPosition();
            RecordsPlayerActivity.timeText.setText(convertMillisecToTimeString(liveStreamTime));
            if (selectedStreamTime != 0){
                if (convertMillisecToTimeString(liveStreamTime).equals(convertMillisecToTimeString(selectedStreamTime))){
                    myHandler.removeCallbacks(this);
                    stopMedia();
                }else {
                    myHandler.postDelayed(this, 1000);
                }
            }else {
                myHandler.postDelayed(this, 1000);
            }

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null){
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        removeNotification();
        if (phoneStateListener != null){
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        myHandler.removeCallbacks(UpdateSongTime);
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
    }
}

