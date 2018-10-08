package io.adie.nowplayingnotify.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

import io.adie.nowplayingnotify.R;
import io.adie.nowplayingnotify.receiver.AudioStateReceiver;

@RequiresApi(api = Build.VERSION_CODES.N)
public class NowPlayingTileService extends TileService {
    public static final String NOWPLAYING_NOTIFICATION_CHANNEL_ID = "NOW_PLAYING";
    private static final String STOP_ACTION = "STOP_NOTIFICATIONS_ACTION";
    private static final int NOTIFICATION_ID = 696;
    private static final String PERSISTENT_NOTIFICATION_CHANNEL_ID = "PERSIST";
    private boolean _active = false;
    private AudioStateReceiver _audioReceiver;
    private Notification notification;

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        updateTileState();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel persistentChannel = new NotificationChannel(PERSISTENT_NOTIFICATION_CHANNEL_ID, getString(R.string.notification_persist_title), NotificationManager.IMPORTANCE_LOW);
            NotificationChannel nowPlayingChannel = new NotificationChannel(NOWPLAYING_NOTIFICATION_CHANNEL_ID, getString(R.string.notification_now_playing_title), NotificationManager.IMPORTANCE_HIGH); // TODO: 10/8/2018 Make this important configurable ;

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(persistentChannel);
                notificationManager.createNotificationChannel(nowPlayingChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_active && _audioReceiver != null) {
            unregisterReceiver(_audioReceiver);
        }
        stopForeground(true);
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        if (_active) {
            _active = false;
            stopForeground(true);
            updateTileState();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        _active = !_active;
        updateTileState();

        if (_active) {
            start();
        } else {
            stop();
        }
    }

    private void start() {
        startPersistent();
        // Start listener
        final IntentFilter intentFilter = new IntentFilter("com.android.music.metachanged");
        intentFilter.addAction("com.spotify.music.metadatachanged");
        intentFilter.addAction("com.spotify.music.metachanged");
        _audioReceiver = new AudioStateReceiver();
        registerReceiver(_audioReceiver, intentFilter);
    }

    private void stop() {
        stopForeground(true);
        // Stop listener
        if (_audioReceiver != null) {
            unregisterReceiver(_audioReceiver);
        }
    }

    void updateTileState() {
        final Tile tile = getQsTile();
        if (tile != null) {
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_icon));
            tile.setState(_active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int retVal = super.onStartCommand(intent, flags, startId);

        if (STOP_ACTION.equals(intent.getAction())) {
            stop();
        }


        return retVal;
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
    }
}
