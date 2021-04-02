package it.contact.tracing;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import it.contact.tracing.service.MyService;

public class MainActivity extends FlutterActivity {

    private Intent forService;

    @Override
    public void configureFlutterEngine(@NonNull final FlutterEngine flutterEngine) {

        GeneratedPluginRegistrant.registerWith(flutterEngine);
        forService = new Intent(MainActivity.this, MyService.class);
        startForegroundService(forService);
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//
//        forService = new Intent(MainActivity.this, MyService.class);
//        startForegroundService(forService);
//    }
}
