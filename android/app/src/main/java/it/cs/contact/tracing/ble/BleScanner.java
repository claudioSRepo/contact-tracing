package it.cs.contact.tracing.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import it.cs.contact.tracing.config.InternalConfig;

public class BleScanner {

    public static final String TAG = "BleScanner";

    final BleScanCallbackHandler scanCallback = new BleScanCallbackHandler();

    public void scan(final Context context) {

        Log.i(TAG, "Started background process...");
        final BluetoothAdapter blAdapter = BluetoothAdapter.getDefaultAdapter();

        if (blAdapter != null && blAdapter.isEnabled()) {

            startBleScan(blAdapter, context);
        } else {
            Log.i(TAG, "Bluetooth disabled");
        }
    }

    private void startBleScan(final BluetoothAdapter blAdapter, final Context context) {

        Log.i(TAG, "Started BLE Adapter");

        final BluetoothLeScanner bluetoothLeScanner = blAdapter.getBluetoothLeScanner();

        final Handler handler = new Handler();

        if (bluetoothLeScanner != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            stopIfExists(bluetoothLeScanner);

            final ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setMatchMode(ScanSettings.MATCH_MODE_STICKY).build();

            handler.postDelayed(() -> bluetoothLeScanner.stopScan(scanCallback), InternalConfig.BLE_SCAN_PERIOD);

            Log.i(TAG, "BLE available, starting scanner...");
            //ServiceUuid(new ParcelUuid(InternalConfig.BLE_KEY_EXCHANGE_SERVICE_UUID)
            final List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().setDeviceName("OnePlus 8 Pro").build());

            bluetoothLeScanner.startScan(filters, settings, scanCallback);


        } else {
            Log.i(TAG, "BLE not available");
        }
    }

    private void stopIfExists(final BluetoothLeScanner bluetoothLeScanner) {
        try {
            scanCallback.clear();
            bluetoothLeScanner.stopScan(scanCallback);
        } catch (final Exception ignored) {
        }
    }
}
