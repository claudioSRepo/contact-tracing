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
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;
import it.cs.contact.tracing.flutter.UiRiskProvider;
import it.cs.contact.tracing.foreground.BlForegroundService;

public class MainActivity extends FlutterActivity {

    public static final String TAG = "MainActivity";

    private static final String GET_RISK_CHANNEL = "it.cs.contact.tracing/getRisk";

    @Override
    public void configureFlutterEngine(@NonNull final FlutterEngine flutterEngine) {

        super.configureFlutterEngine(flutterEngine);
        GeneratedPluginRegistrant.registerWith(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), GET_RISK_CHANNEL)
                .setMethodCallHandler(UiRiskProvider.getRiskSummaryHandler);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Creating main activity");

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS}, 456348);

        startForegroundService();
    }

    private void startForegroundService() {

        Log.i(TAG, "Starting foreground service");

        final Intent forService = new Intent(MainActivity.this, BlForegroundService.class);
        forService.setAction(BlForegroundService.ACTION_INIT);
        startForegroundService(forService);
    }
}
