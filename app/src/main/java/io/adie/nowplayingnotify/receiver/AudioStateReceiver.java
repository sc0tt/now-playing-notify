package io.adie.nowplayingnotify.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import io.adie.nowplayingnotify.R;
import io.adie.nowplayingnotify.service.NowPlayingService;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AudioStateReceiver extends BroadcastReceiver {
    private static final String TAG = AudioStateReceiver.class.getSimpleName();
    private static final int NOTIFICATION_ID = 4242;
    private static final long NOTIFICATION_DURATION_MS = TimeUnit.SECONDS.toMillis(5);
    private static Handler mHandler;

    public AudioStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        boolean playing = intent.getBooleanExtra("playing", false);
        if (!playing) {
            return;
        }

        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        Long albumID = intent.getLongExtra("albumId", 0);

        final String desc = String.format("by %s on %s", artist, album);

        final Notification.Builder builder = new Notification.Builder(context.getApplicationContext(),
                NowPlayingService.NOWPLAYING_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(track)
                .setContentText(desc)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setSmallIcon(R.drawable.ic_persist)
                .setLocalOnly(true)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        if (AudioStateReceiver.mHandler != null) {
            AudioStateReceiver.mHandler.removeCallbacksAndMessages(null);
        }

        AudioStateReceiver.mHandler = new Handler();
        AudioStateReceiver.mHandler.postDelayed(() -> notificationManager.cancel(NOTIFICATION_ID),
                NOTIFICATION_DURATION_MS);
    }
}
