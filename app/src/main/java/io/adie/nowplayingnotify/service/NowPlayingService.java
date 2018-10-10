package io.adie.nowplayingnotify.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.adie.nowplayingnotify.R;
import io.adie.nowplayingnotify.receiver.AudioStateReceiver;

public class NowPlayingService extends Service {
    public static final String NOWPLAYING_NOTIFICATION_CHANNEL_ID = "NOW_PLAYING";
    public static final String START_ACTION = "START_NOW_ACTION";
    public static final String STOP_ACTION = "STOP_NOTIFICATIONS_ACTION";
    private static final int NOTIFICATION_ID = 696;
    private static final String PERSISTENT_NOTIFICATION_CHANNEL_ID = "PERSIST";
    private static final String TAG = "NowPlayingService";
    private final IBinder _binder = new LocalBinder();
    private AudioStateReceiver _audioReceiver;
    private Notification notification;
    private boolean _active = false;
    private MutableLiveData<Boolean> _activeState = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel persistentChannel = new NotificationChannel(PERSISTENT_NOTIFICATION_CHANNEL_ID, getString(R.string.notification_persist_title), NotificationManager.IMPORTANCE_LOW);
        NotificationChannel nowPlayingChannel = new NotificationChannel(NOWPLAYING_NOTIFICATION_CHANNEL_ID, getString(R.string.notification_now_playing_title), NotificationManager.IMPORTANCE_HIGH);
        nowPlayingChannel.setSound(null, null);
        nowPlayingChannel.enableLights(false);
        nowPlayingChannel.enableVibration(false);

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

        if (intent != null) {
            if (STOP_ACTION.equals(intent.getAction()) && _active) {
                stop();
            } else if (START_ACTION.equals(intent.getAction()) && !_active) {
                start();
            }
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
        Intent stopIntent = new Intent(this, NowPlayingTileService.class);
        stopIntent.setAction(STOP_ACTION);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_notification))
                .addAction(R.drawable.ic_stop_black_24dp, getString(R.string.notification_stop), stopPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(PERSISTENT_NOTIFICATION_CHANNEL_ID);
        }

        notification = notificationBuilder.build();
        startForeground(NOTIFICATION_ID, notification);

        final IntentFilter intentFilter = new IntentFilter("com.android.music.metachanged");
        intentFilter.addAction("com.spotify.music.metadatachanged");
        intentFilter.addAction("com.spotify.music.metachanged");
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

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
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
