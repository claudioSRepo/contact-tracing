package it.cs.contact.tracing;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import it.cs.contact.tracing.service.BlForegroundService;

public class MainActivity extends FlutterActivity {

    public static final String TAG = "MainActivity";

    @Override
    public void configureFlutterEngine(@NonNull final FlutterEngine flutterEngine) {

        GeneratedPluginRegistrant.registerWith(flutterEngine);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Creating main activity");

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 34242);

        startForegroundService();
    }

    private void startForegroundService() {

        Log.i(TAG, "Starting foreground service");

        final Intent forService = new Intent(MainActivity.this, BlForegroundService.class);
        startForegroundService(forService);
    }
}
