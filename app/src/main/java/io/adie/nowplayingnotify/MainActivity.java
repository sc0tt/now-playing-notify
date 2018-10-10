package io.adie.nowplayingnotify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import io.adie.nowplayingnotify.service.NowPlayingService;

public class MainActivity extends AppCompatActivity implements NowPlayingService.NowPlayingCallback {

    private boolean _bound = false;
    private NowPlayingService _service;

    private BooleanPreference _enabled;

    private ServiceConnection _connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            final NowPlayingService.LocalBinder bind = (NowPlayingService.LocalBinder) service;
            _service = bind.getService();
            _service.registerCallback(MainActivity.this);
            _bound = true;
            updateEnabledStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _bound = false;
            updateEnabledStatus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        _enabled = findViewById(R.id.app_enabled);
        _enabled.setOnClickListener(v -> {
            final boolean newState = !((boolean) v.getTag());
            if (_bound) {
                if (newState) {
                    _service.start();
                } else {
                    _service.stop();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _enabled.setOnClickListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, NowPlayingService.class);
        bindService(intent, _connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void currentStatus(boolean active) {
        _enabled.setChecked(active);
    }

    @Override
    protected void onStop() {
        super.onStop();
        _service.removeCallback(this);
        unbindService(_connection);
        _bound = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_oss:
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateEnabledStatus() {
        _enabled.setChecked(_bound && _service.isActive());
    }
}
