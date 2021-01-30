package abdoroid.quranradio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import abdoroid.quranradio.R;
import abdoroid.quranradio.ui.player.PlayerActivity;

public class MediaPlaybackService extends MediaBrowserServiceCompat implements
        MediaPlayerCallback.PlayerListener, AudioManager.OnAudioFocusChangeListener {

    private static final String MEDIA_EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private static final String PLAYER_NOTIFICATION_CHANNEL_ID = "player_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String LOG_TAG = MediaBrowserServiceCompat.class.getName();

    private MediaSessionCompat mediaSession;
    private AudioFocusRequest focusRequest;
    private AudioManager audioManager;
    private boolean paused = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        createMediaSession();
        registerBecomingNoisyReceiver();
        requestAudioFocus();
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_EMPTY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        if (parentId.equals(MEDIA_EMPTY_MEDIA_ROOT_ID)){
            result.sendResult(null);
        }
    }

    @SuppressWarnings( "deprecation" )
    private void createMediaSession(){
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            mediaSession.setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        }
        setSessionToken(mediaSession.getSessionToken());
        MediaPlayerCallback callback = new MediaPlayerCallback(this, mediaSession);
        callback.listener = this;
        mediaSession.setCallback(callback);
    }

    private boolean isPlaying(){
        if (mediaSession.getController().getPlaybackState() != null){
            return mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
        }else {
            return false;
        }
    }

    private PendingIntent getNotificationIntent(){
        Intent openActivityIntent = new Intent(this, PlayerActivity.class);
        openActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                MediaPlaybackService.this, 0,
                openActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager.getNotificationChannel(PLAYER_NOTIFICATION_CHANNEL_ID) == null){
                NotificationChannel notificationChannel =
                        new NotificationChannel(PLAYER_NOTIFICATION_CHANNEL_ID,
                                "player",
                                NotificationManager.IMPORTANCE_LOW);
                manager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private Notification createNotification(Bitmap bitmap){
        PendingIntent notificationIntent = getNotificationIntent();
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction;
        if (isPlaying()) {
            play_pauseAction = MediaButtonReceiver.buildMediaButtonPendingIntent(MediaPlaybackService.this,
                    PlaybackStateCompat.ACTION_PAUSE);
        } else  {
            notificationAction = android.R.drawable.ic_media_play;
            play_pauseAction = MediaButtonReceiver.buildMediaButtonPendingIntent(MediaPlaybackService.this,
                    PlaybackStateCompat.ACTION_PLAY);
        }

        MediaDescriptionCompat description = mediaSession.getController().getMetadata().getDescription();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), PLAYER_NOTIFICATION_CHANNEL_ID);
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0,1,2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_STOP)))
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.smallicon)
                .setContentText(description.getTitle())
                .setContentIntent(notificationIntent)
                .setLargeIcon(bitmap)
                .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous, "back", MediaButtonReceiver.buildMediaButtonPendingIntent(MediaPlaybackService.this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", MediaButtonReceiver.buildMediaButtonPendingIntent(MediaPlaybackService.this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        return builder.build();
    }

    @SuppressWarnings("deprecation")
    private void displayNotification(){
        if (mediaSession.getController().getMetadata() == null){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel();
        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.largicon);
        Notification notification = createNotification(largeIcon);
        ContextCompat.startForegroundService(MediaPlaybackService.this,
                new Intent(MediaPlaybackService.this, MediaPlaybackService.class));
        startForeground(NOTIFICATION_ID, notification);
    }

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaSession.getController().getTransportControls().pause();
        }
    };

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (paused) {
                    mediaSession.getController().getTransportControls().play();
                    paused = false;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (isPlaying()){
                    paused = true;
                    mediaSession.getController().getTransportControls().pause();
                }
                handler.postDelayed(() -> mediaSession.getController().getTransportControls().stop(), TimeUnit.SECONDS.toMillis(60));
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying()){
                    paused = true;
                    mediaSession.getController().getTransportControls().pause();
                }
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mediaSession.getController().getTransportControls().stop();
    }

    @Override
    public void onStateChange() {
        displayNotification();
    }

    @Override
    public void onStopPlaying() {
        stopSelf();
        stopForeground(true);
        removeAudioFocus();
        unregisterReceiver(becomingNoisyReceiver);
    }

    @Override
    public void onPausePlaying() {
        stopForeground(false);
    }

}