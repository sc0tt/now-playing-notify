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
import androidx.annotation.RequiresApi;

import io.adie.nowplayingnotify.R;

@RequiresApi(api = Build.VERSION_CODES.N)
public class NowPlayingTileService extends TileService implements NowPlayingService.NowPlayingCallback {
    private boolean _active = false;
    private NowPlayingService _service;
    private boolean _bound = false;

    private ServiceConnection _serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final NowPlayingService.LocalBinder bind = (NowPlayingService.LocalBinder) service;
            _service = bind.getService();
            _service.registerCallback(NowPlayingTileService.this);
            _bound = true;
            updateTileState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _bound = false;
            updateTileState();
        }
    };

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
    public void onTileRemoved() {
        super.onTileRemoved();
        if (_active) {
            _active = false;
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        _service.removeCallback(this);
    }

    @Override
    public void currentStatus(boolean active) {
        updateTileState();
    }

    private void start() {
        // Bind to service
        final Intent intent = new Intent(this, NowPlayingService.class);
        intent.setAction(NowPlayingService.START_ACTION);
        startService(intent);
        bindService(intent, _serviceConnection, Context.BIND_AUTO_CREATE);
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
