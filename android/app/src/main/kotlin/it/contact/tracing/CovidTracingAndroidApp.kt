package it.contact.tracing

import android.app.NotificationChannel
import android.app.NotificationManager
import io.flutter.app.FlutterApplication

class CovidTracingAndroidApp : FlutterApplication() {

    public override fun onCreate() {

        super.onCreate()

        //Create a channel
        val channel: NotificationChannel = NotificationChannel("messages", "Messages", NotificationManager.IMPORTANCE_LOW);
        val manager: NotificationManager = getSystemService(NotificationManager::class.java);
        manager.createNotificationChannel(channel);
    }

}