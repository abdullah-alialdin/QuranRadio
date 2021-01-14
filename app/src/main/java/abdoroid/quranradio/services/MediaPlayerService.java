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
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.util.ArrayList;

import abdoroid.quranradio.R;
import abdoroid.quranradio.pojo.RadioDataModel;
import abdoroid.quranradio.utils.PlaybackStatus;
import abdoroid.quranradio.ui.player.PlayerActivity;
import abdoroid.quranradio.utils.StorageUtils;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "abdoroid.quranradio.ui.player.ACTION_PLAY";
    public static final String ACTION_PAUSE = "abdoroid.quranradio.ui.player.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "abdoroid.quranradio.ui.player.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "abdoroid.quranradio.ui.player.ACTION_NEXT";
    public static final String ACTION_CANCEL = "abdoroid.quranradio.ui.player.ACTION_CANCEL";
    public static final String DELETE_INTENT_KEY = "deleteIntent";


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
    public boolean isPaused;
    private AudioFocusRequest focusRequest;
    private AudioAttributes audioAttributes;
    private int stopCode;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            stopCode = intent.getExtras().getInt(DELETE_INTENT_KEY);
            if (stopCode == 1) {
                pauseMedia();
                stopCode = 0;
            }
        }
        try {
            StorageUtils storage = new StorageUtils(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }
        requestAudioFocus();
        if (mediaSessionManager == null) {
            try{
                initMediaPlayer();
                initMediaSession();
                buildNotification(PlaybackStatus.PLAYING);
            }catch (NullPointerException e){
                e.printStackTrace();
                stopSelf();
            }
        }

        handleIncomingActions(intent);
        return START_STICKY;
    }

    private void initMediaPlayer() {
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
        }
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.prepareAsync();
    }

    public void playMedia() {
        if (!isPng() && mediaPlayer != null) {
            mediaPlayer.start();
            buildNotification(PlaybackStatus.PLAYING);
            isPaused = false;
        }
        if (isDestroyed()){
            updateMetaData();
            requestAudioFocus();
            buildNotification(PlaybackStatus.PLAYING);
            stopMedia();
            if (mediaPlayer != null){
                mediaPlayer.reset();
            }
            initMediaPlayer();
        }
    }

    public void pauseMedia() {
        if (isPng()) {
            mediaPlayer.pause();
            isPaused = true;
            if (stopCode != 1) buildNotification(PlaybackStatus.PAUSED);
        }
    }

    public void stopMedia() {
        if (mediaPlayer == null) return;
        if (isPng()) {
            mediaPlayer.stop();
        }
    }

    public boolean isPng() {
        if (mediaPlayer != null){
            return mediaPlayer.isPlaying();
        }else{
            return false;
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
        Toast.makeText(getApplicationContext(), getString(R.string.maintenance_msg), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(getApplicationContext(),
                        "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra,
                        Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(getApplicationContext(),
                        "MEDIA ERROR SERVER DIED " + extra, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(getApplicationContext(),
                        "MEDIA ERROR UNKNOWN " + extra, Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                PlayerActivity.mVisualizer.setAudioSessionId(getSessionId());
            }catch(NullPointerException e){
                e.printStackTrace();
                stopSelf();
            }

        }
    }

    public String getPlayingNow() {
        return activeAudio.getName();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        boolean pause = false;
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (pause){
                    if (mediaPlayer == null) {
                        initMediaPlayer();
                    } else if (!isPng()) {
                        playMedia();
                    }
                }
                if (mediaPlayer != null){
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    isPaused = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPng()) {
                    pauseMedia();
                    pause = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (isPng()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @SuppressWarnings("deprecation")
    private void requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(audioAttributes)
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

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
        }
    };

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null && isPng()) {
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
            audioIndex = new StorageUtils(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
            stopMedia();
            if (mediaPlayer != null){
                mediaPlayer.reset();
            }
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void registerPlayNewAudio() {
        IntentFilter intentFilter = new IntentFilter(PlayerActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, intentFilter);
    }

    private void initMediaSession() {
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
                pauseMedia();
                removeNotification();
            }
        });

    }

    private void updateMetaData() {
        mediaSession.setMetadata(new MediaMetadataCompat.Builder().
                putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getName()).build());
    }

    public void skipToNext() {
        if (audioIndex == audioList.size() - 1) {
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(++audioIndex);
        }
        new StorageUtils(getApplicationContext()).storeAudioIndex(audioIndex);
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        stopMedia();
        if (mediaPlayer != null){
            mediaPlayer.reset();
        }
        initMediaPlayer();
    }

    public void skipToPrevious() {
        if (audioIndex == 0) {
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(--audioIndex);
        }
        new StorageUtils(getApplicationContext()).storeAudioIndex(audioIndex);
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        stopMedia();
        if (mediaPlayer != null){
            mediaPlayer.reset();
        }
        initMediaPlayer();
    }

    public int getSessionId() {
        if (isPng()) {
            return mediaPlayer.getAudioSessionId();
        }
        return 0;
    }

    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;
        if (playbackStatus == PlaybackStatus.PLAYING) {
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            play_pauseAction = playbackAction(0);
        }

        createNotificationChannel();
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
        deleteIntent.putExtra(DELETE_INTENT_KEY, 1);
        PendingIntent deletePendingIntent = PendingIntent.getService(this, 1,
                deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.largicon);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2))
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.smallicon)
                .setContentText(activeAudio.getName())
                .setContentIntent(pendingIntent)
                .setLargeIcon(largeIcon)
                .setDeleteIntent(deletePendingIntent)
                .addAction(android.R.drawable.ic_media_previous, "previous",
                        playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next",
                        playbackAction(2))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "cancel", playbackAction(4));

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Media playback";
            String des = "Media playback controls";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(des);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void removeNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
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
            case 4:
                playbackAction.setAction(ACTION_CANCEL);
                return PendingIntent.getService(this, actionNumber, playbackAction, PendingIntent.FLAG_CANCEL_CURRENT);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
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
        } else if (actionString.equalsIgnoreCase(ACTION_CANCEL)) {
            transportControls.stop();
        }
    }

    public long getMediaPosition(){
        if (mediaPlayer != null){
            return mediaPlayer.getCurrentPosition();
        }else {
            return 0;
        }

    }

    public boolean isDestroyed(){
        return mediaPlayer == null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        removeNotification();
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
        new StorageUtils(getApplicationContext()).clearCachedAudioPlaylist();
    }
}

