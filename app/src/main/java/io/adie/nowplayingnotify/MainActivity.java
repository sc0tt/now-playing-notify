package io.adie.nowplayingnotify;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import androidx.appcompat.app.AppCompatActivity;
import io.adie.nowplayingnotify.service.NowPlayingService;

public class MainActivity extends AppCompatActivity {

    private boolean _bound = false;
    private NowPlayingService _service;

    private BooleanPreference _enabled;

    private final ServiceConnection _connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            final NowPlayingService.LocalBinder bind = (NowPlayingService.LocalBinder) service;
            _service = bind.getService();
            _service.isActive().observe(MainActivity.this, MainActivity.this::updateEnabledStatus);
            _bound = true;
            updateStatus();
        }

        private void updateStatus() {
            final Boolean serviceResult = _service.isActive().getValue();
            final boolean currentStatus = serviceResult == null ? false : serviceResult;
            updateEnabledStatus(currentStatus);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _bound = false;
            updateStatus();
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
        startService(intent);
        bindService(intent, _connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
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

        if (item.getItemId() == R.id.action_oss) {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateEnabledStatus(final boolean active) {
        _enabled.setChecked(active);
    }
}
