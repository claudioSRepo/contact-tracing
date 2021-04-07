package it.cs.contact.tracing;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import it.cs.contact.tracing.service.MyService;

public class MainActivity extends FlutterActivity {

    private Intent forService;

    @Override
    public void configureFlutterEngine(@NonNull final FlutterEngine flutterEngine) {

        GeneratedPluginRegistrant.registerWith(flutterEngine);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 34242);

        forService = new Intent(MainActivity.this, MyService.class);
        startForegroundService(forService);
    }
}
