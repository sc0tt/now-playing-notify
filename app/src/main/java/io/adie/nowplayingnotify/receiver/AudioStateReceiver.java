package io.adie.nowplayingnotify.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import io.adie.nowplayingnotify.R;
import io.adie.nowplayingnotify.service.NowPlayingTileService;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AudioStateReceiver extends BroadcastReceiver {
    private static final String TAG = AudioStateReceiver.class.getSimpleName();

    private static Handler mHandler;
    private NotificationManager mNotifyMgr;

    Runnable removeTask = new Runnable() {
        @Override
        public void run() {
            mNotifyMgr.cancel(1);
        }
    };

    public AudioStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final Boolean app_enabled = sharedPref.getBoolean("app_enabled", false);
        if (!app_enabled) {
            Log.d(TAG, "App disabled, exiting...");
            return;
        }

        mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        boolean playing = intent.getBooleanExtra("playing", false);
        if (!playing) {
            Log.d(TAG, "Not playing");
            return;
        }

        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        Long albumID = intent.getLongExtra("albumId", 0);
        final String desc = String.format("by %s on %s", artist, album);

        final Notification.Builder builder = new Notification.Builder(context.getApplicationContext())
                .setContentTitle(track)
                .setContentText(desc)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setSmallIcon(R.drawable.ic_icon)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(new long[0])
                .setLocalOnly(true)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NowPlayingTileService.NOWPLAYING_NOTIFICATION_CHANNEL_ID);
        }

        mNotifyMgr.notify(1, builder.build());

        if (AudioStateReceiver.mHandler != null) {
            AudioStateReceiver.mHandler.removeCallbacksAndMessages(null);
        }

        AudioStateReceiver.mHandler = new Handler();
        AudioStateReceiver.mHandler.postDelayed(removeTask, 1000 * 5);
    }
}
