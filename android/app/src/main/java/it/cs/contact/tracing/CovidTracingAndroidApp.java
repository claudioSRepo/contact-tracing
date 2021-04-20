package it.cs.contact.tracing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.app.FlutterApplication;

public class CovidTracingAndroidApp extends FlutterApplication {

    private static Context context;

    private static ExecutorService pool;

    @Override
    public void onCreate() {

        super.onCreate();

        context = getApplicationContext();
        pool = Executors.newFixedThreadPool(2);

        //Create a channel when application is started
        final NotificationChannel channel = new NotificationChannel("messages", "Messages", NotificationManager.IMPORTANCE_LOW);
        final NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }


    public static Context getAppContext() {
        return CovidTracingAndroidApp.context;
    }

    public static ExecutorService getThreadPool() {
        return CovidTracingAndroidApp.pool;
    }
}