package it.cs.contact.tracing.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.ble.BleScanner;
import it.cs.contact.tracing.ble.BleGattServer;

public class BlForegroundService extends Service {

    private static final String TAG = "BlForegroundService";

    public static final String ACTION_START = "START";

    public static final String ACTION_EXECUTE_FOREGROUND_SCAN = "EXECUTE_FOREGROUND_SCAN";

    @Override
    public void onCreate() {
        super.onCreate();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "messages").
                setContentText("this is running in background (v7)").
                setContentTitle("flutter bck").
                setOngoing(true).
                setSmallIcon(android.R.drawable.ic_menu_mylocation);

        startForeground(101, builder.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String action = intent.getAction();

        Log.i(TAG, "onStartCommand. Action: " + action);

        switch (Objects.requireNonNull(action)) {

            case ACTION_START:

                startBlClientJob();
                startBlServer();
                break;

            case ACTION_EXECUTE_FOREGROUND_SCAN:
                startScan();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startBlClientJob() {

        Log.i(TAG, "Starting BL client job...");

        final Intent forService = new Intent(this, BlForegroundService.class);
        forService.setAction(ACTION_EXECUTE_FOREGROUND_SCAN);
        final PendingIntent pendingIntent = PendingIntent.getForegroundService(this, 0, forService, 0);

        final AlarmManager alarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000,
                InternalConfig.BL_CHECKER_SCHEDULING_SEC, pendingIntent);
    }

    private void startScan() {
        new BleScanner().scan(this);
    }

    private void startBlServer() {

        Log.i(TAG, "Starting BL server...");
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        CovidTracingAndroidApp.getThreadPool().execute(() -> new BleGattServer(mBluetoothManager).start());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
