package io.adie.nowplayingnotify.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.adie.nowplayingnotify.R;
import io.adie.nowplayingnotify.receiver.AudioStateReceiver;

public class NowPlayingService extends Service {

    private static final String TAG = "NowPlayingService";

    public static final String NOWPLAYING_NOTIFICATION_CHANNEL_ID = "NOW_PLAYING";
    public static final String STOP_ACTION = "STOP_NOTIFICATIONS_ACTION";
    public static final String PERSISTENT_NOTIFICATION_CHANNEL_ID = "PERSIST";
    public static final int PERSISTENT_NOTIFICATION_ID = 322;

    private final IBinder _binder = new LocalBinder();
    private boolean _active = false;
    private final MutableLiveData<Boolean> _activeState = new MutableLiveData<>();

    private AudioStateReceiver _audioReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel persistentChannel = new NotificationChannel(PERSISTENT_NOTIFICATION_CHANNEL_ID, getString(R.string.notification_persist_title), NotificationManager.IMPORTANCE_LOW);
        persistentChannel.setImportance(NotificationManager.IMPORTANCE_LOW);

        NotificationChannel nowPlayingChannel = new NotificationChannel(NOWPLAYING_NOTIFICATION_CHANNEL_ID, getString(R.string.notification_now_playing_title), NotificationManager.IMPORTANCE_HIGH);
        nowPlayingChannel.setSound(null, null);
        nowPlayingChannel.enableLights(false);
        nowPlayingChannel.enableVibration(false);
        nowPlayingChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(persistentChannel);
            notificationManager.createNotificationChannel(nowPlayingChannel);
        }

        _audioReceiver = new AudioStateReceiver();
        _activeState.postValue(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPersistent();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null && STOP_ACTION.equals(intent.getAction()) && _active) {
            stop();
        }
        return START_STICKY;
    }

    public void start() {
        startPersistent();
        _active = true;
        _activeState.postValue(true);
    }

    public void stop() {
        stopPersistent();
        _active = false;
        _activeState.postValue(false);
    }

    private void startPersistent() {
        Intent stopIntent = new Intent(this, NowPlayingService.class);
        stopIntent.setAction(STOP_ACTION);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);


        final Notification.Action stopAction = new Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_stop_black_24dp),
                getString(R.string.notification_stop),
                stopPendingIntent).build();
        Notification.Builder notificationBuilder = new Notification.Builder(this, PERSISTENT_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_persist)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_notification))
                .addAction(stopAction);

        Notification notification = notificationBuilder.build();
        startForeground(PERSISTENT_NOTIFICATION_ID, notification);

        final String[] actions = new String[]{
                "com.android.music.metachanged",
                "com.htc.music.metachanged",
                "fm.last.android.metachanged",
                "com.sec.android.app.music.metachanged",
                "com.nullsoft.winamp.metachanged",
                "com.amazon.mp3.metachanged",
                "com.miui.player.metachanged",
                "com.real.IMP.metachanged",
                "com.sonyericsson.music.metachanged",
                "com.rdio.android.metachanged",
                "com.samsung.sec.android.MusicPlayer.metachanged",
                "com.andrew.apollo.metachanged",
                "com.spotify.music.metadatachanged",
                "com.spotify.music.metachanged",
        };
        final IntentFilter intentFilter = new IntentFilter();
        for (final String action : actions) {
            intentFilter.addAction(action);
        }
        _audioReceiver = new AudioStateReceiver();
        registerReceiver(_audioReceiver, intentFilter);
    }

    private void stopPersistent() {
        if (_audioReceiver != null) {
            try {
                unregisterReceiver(_audioReceiver);
            } catch (final IllegalArgumentException ex) {
                Log.e(TAG, "Error unregistering", ex);
            }
            _audioReceiver = null;
        }

        stopForeground(true);
    }

    public LiveData<Boolean> isActive() {
        return _activeState;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    public class LocalBinder extends Binder {

        public NowPlayingService getService() {
            return NowPlayingService.this;
        }
    }
}
