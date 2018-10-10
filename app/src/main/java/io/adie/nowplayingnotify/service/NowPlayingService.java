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
import android.support.annotation.Nullable;

import io.adie.nowplayingnotify.R;
import io.adie.nowplayingnotify.receiver.AudioStateReceiver;

public class NowPlayingService extends Service {
    public static final String NOWPLAYING_NOTIFICATION_CHANNEL_ID = "NOW_PLAYING";
    public static final String START_ACTION = "START_NOW_ACTION";
    public static final String STOP_ACTION = "STOP_NOTIFICATIONS_ACTION";
    private static final int NOTIFICATION_ID = 696;
    private static final String PERSISTENT_NOTIFICATION_CHANNEL_ID = "PERSIST";
    private final IBinder _binder = new LocalBinder();
    private AudioStateReceiver _audioReceiver;
    private Notification notification;
    private boolean active = false;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (STOP_ACTION.equals(intent.getAction()) && active) {
            stopPersistent();
            active = false;
        } else if (START_ACTION.equals(intent.getAction()) && !active) {
            startPersistent();
            active = true;
        }

        return START_STICKY;
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

    public void stopPersistent() {
        if (_audioReceiver != null) {
            unregisterReceiver(_audioReceiver);
        }

        stopForeground(true);
    }

    public boolean isActive() {
        return active;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    public class LocalBinder extends Binder {

        NowPlayingService getService() {
            return NowPlayingService.this;
        }
    }
}
