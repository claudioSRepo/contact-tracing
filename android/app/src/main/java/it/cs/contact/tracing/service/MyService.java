package it.cs.contact.tracing.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import it.cs.contact.tracing.receiver.BlBackgroundJobHandler;
import it.cs.contact.tracing.receiver.BlDiscoveryHandler;

public class MyService extends Service {

    private BlDiscoveryHandler blDiscoveryHandler = null;

    @Override
    public void onCreate() {
        super.onCreate();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "messages").
                setContentText("this is running in background (v4)").
                setContentTitle("flutter bck").
                setOngoing(true).
                setSmallIcon(android.R.drawable.ic_menu_mylocation);

        startForeground(101, builder.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("FGActivity", "MyService.onStartCommand");

        initBlReceivers();

        final Intent notificationIntent = new Intent(this, BlBackgroundJobHandler.class);
        final PendingIntent intent2 =
                PendingIntent.getBroadcast(this, 1002, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        final AlarmManager alarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000,
                AlarmManager.INTERVAL_HOUR / 60 / 2, intent2);

        return super.onStartCommand(intent, flags, startId);
    }


    private void initBlReceivers() {

        if (blDiscoveryHandler == null) {

            IntentFilter filter;
            blDiscoveryHandler = new BlDiscoveryHandler();

            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getApplicationContext().registerReceiver(blDiscoveryHandler, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            getApplicationContext().registerReceiver(blDiscoveryHandler, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            getApplicationContext().registerReceiver(blDiscoveryHandler, filter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (blDiscoveryHandler != null) {
            getApplicationContext().unregisterReceiver(blDiscoveryHandler);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
