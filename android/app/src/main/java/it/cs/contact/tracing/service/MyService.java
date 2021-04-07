package it.cs.contact.tracing.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import it.cs.contact.tracing.ActionHandler;

public class MyService extends Service {

    private final IBinder binder = new AppServiceBinder();

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

        final Intent notificationIntent = new Intent(this, ActionHandler.class);
        final PendingIntent intent2 =
                PendingIntent.getBroadcast(this, 1002, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        final AlarmManager alarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000,
                AlarmManager.INTERVAL_HOUR / 60 / 4, intent2);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }


    public class AppServiceBinder extends Binder {

        MyService getService() {
            return MyService.this;
        }
    }
}
