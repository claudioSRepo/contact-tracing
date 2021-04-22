package it.cs.contact.tracing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.provider.ContactsContract;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.app.FlutterApplication;
import it.cs.contact.tracing.config.Database;

public class CovidTracingAndroidApp extends FlutterApplication {

    private static Context context;

    private static ExecutorService pool;

    private static Database db;

    @Override
    public void onCreate() {

        super.onCreate();

        initAll();
        createForegroundChannel();
    }

    private void createForegroundChannel() {

        //Create a channel when application is started
        final NotificationChannel channel = new NotificationChannel("messages", "Messages", NotificationManager.IMPORTANCE_LOW);
        final NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private void initAll() {

        context = getApplicationContext();
        pool = Executors.newFixedThreadPool(2);
        db = Database.getInstance(context);
    }

    public static Context getAppContext() {
        return CovidTracingAndroidApp.context;
    }

    public static ExecutorService getThreadPool() {
        return CovidTracingAndroidApp.pool;
    }

    public static synchronized Database getDb() {

        return db;
    }
}