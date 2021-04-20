package it.cs.contact.tracing.handler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.cs.contact.tracing.config.InternalConfig;

public class BlBackgroundJobHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.i("BlBackgroundJobHandler", "Started background process...");
        final BluetoothAdapter blAdapter = BluetoothAdapter.getDefaultAdapter();

        if (blAdapter != null && blAdapter.isEnabled()) {
//            startBlScan(blAdapter);
            startBleScan(blAdapter, context);
        } else {
            Log.i("BlBackgroundJobHandler", "Bluetooth disabled");
        }
    }

    private void startBlScan(final BluetoothAdapter blAdapter) {

        Log.i("BlBackgroundJobHandler", "Started BL Adapter");

        boolean started = blAdapter.startDiscovery();
        Log.i("BlBackgroundJobHandler", "BL Discovery started: " + started);

    }

    private void startBleScan(final BluetoothAdapter blAdapter, final Context context) {

        Log.i("BlBackgroundJobHandler", "Started BLE Adapter");

        final BluetoothLeScanner bluetoothLeScanner = blAdapter.getBluetoothLeScanner();
        final BluetoothLeDiscoveryHandler scanCallback = new BluetoothLeDiscoveryHandler();

        final Handler handler = new Handler();

        if (bluetoothLeScanner != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            final ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).setMatchMode(ScanSettings.MATCH_MODE_STICKY).build();

            handler.postDelayed(() -> bluetoothLeScanner.stopScan(scanCallback), InternalConfig.BLE_SCAN_PERIOD);

            Log.i("BlBackgroundJobHandler", "BLE available, starting scanner..");
            final List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(InternalConfig.BLE_KEY_EXCHANGE_SERVICE_UUID)).build());

            bluetoothLeScanner.startScan(filters, settings, scanCallback);

        } else {
            Log.i("BlBackgroundJobHandler", "BLE not available");
        }
    }
}
