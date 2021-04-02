package it.contact.tracing.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "messages").
                setContentText("this is running in background").
                setContentTitle("flutter bck").
                setSmallIcon(android.R.drawable.ic_dialog_dialer);

        startForeground(101, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
