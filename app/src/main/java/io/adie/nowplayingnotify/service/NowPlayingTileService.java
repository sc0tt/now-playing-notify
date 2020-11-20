package io.adie.nowplayingnotify.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ServiceLifecycleDispatcher;
import io.adie.nowplayingnotify.R;

@RequiresApi(api = Build.VERSION_CODES.N)
public class NowPlayingTileService extends TileService implements LifecycleOwner {
    private static final String TAG = TileService.class.getSimpleName();
    private final ServiceLifecycleDispatcher _dispatcher = new ServiceLifecycleDispatcher(this);

    private boolean _active = false;
    private NowPlayingService _service;
    private boolean _bound = false;

    private final ServiceConnection _serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final NowPlayingService.LocalBinder bind = (NowPlayingService.LocalBinder) service;
            _service = bind.getService();
            _bound = true;
            _service.isActive().observe(NowPlayingTileService.this, newStatus -> {
                if (newStatus != null) {
                    _active = newStatus;
                    updateTileState();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _bound = false;
            _active = false;
            updateTileState();
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();

    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        unbindService(_serviceConnection);
        updateTileState();
    }

    @Override
    public void onTileRemoved() {
        if (_active) {
            _active = false;
            updateTileState();
        }
        super.onTileRemoved();
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

    @Override
    public void onCreate() {
        _dispatcher.onServicePreSuperOnCreate();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _dispatcher.onServicePreSuperOnDestroy();
        if (_bound) {
            _bound = false;
            try {
                unbindService(_serviceConnection);
            } catch (final IllegalStateException ex) {
                Log.e(TAG, "Error stopping Now Playing Service", ex);
            }
        }
    }

    @Override
    public Lifecycle getLifecycle() {
        return _dispatcher.getLifecycle();
    }

    @CallSuper
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        _dispatcher.onServicePreSuperOnBind();
        return super.onBind(intent);
    }

    @SuppressWarnings("deprecation")
    @CallSuper
    @Override
    public void onStart(Intent intent, int startId) {
        _dispatcher.onServicePreSuperOnStart();
        super.onStart(intent, startId);
    }

    private void start() {
        // Bind to service
        final Intent intent = new Intent(this, NowPlayingService.class);
        intent.setAction(NowPlayingService.START_ACTION);

        try {
            startService(intent);
            bindService(intent, _serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (final IllegalStateException ex) {
            Log.e(TAG, "Error starting Now Playing Service", ex);
            _active = false;
            updateTileState();
        }
    }

    private void stop() {
        if (_bound) {
            _service.stop();
        }
    }

    void updateTileState() {
        final Tile tile = getQsTile();
        if (tile != null) {
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_icon));
            final int state = _active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;

            tile.setState(state);
            tile.updateTile();
        }
    }
}
