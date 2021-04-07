package it.cs.contact.tracing;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import io.flutter.app.FlutterApplication;

public class CovidTracingAndroidApp extends FlutterApplication {

    @Override
    public void onCreate() {

        super.onCreate();

        //Create a channel
        final NotificationChannel channel = new NotificationChannel("messages", "Messages", NotificationManager.IMPORTANCE_LOW);
        final NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
}