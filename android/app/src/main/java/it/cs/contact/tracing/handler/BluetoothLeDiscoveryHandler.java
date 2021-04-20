package it.cs.contact.tracing.handler;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.HashSet;
import java.util.List;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.bl.BluetoothDeviceTracer;
import it.cs.contact.tracing.model.enums.BlType;

import static it.cs.contact.tracing.config.InternalConfig.BLE_KEY_EXCHANGE_CHARACTERISTIC_UUID;
import static it.cs.contact.tracing.config.InternalConfig.BLE_KEY_EXCHANGE_SERVICE_UUID;

public class BluetoothLeDiscoveryHandler extends ScanCallback {

    public static final String BL_DISCOVERY_HANDLER = "BluetoothLeDiscoveryHandler";


    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);

        if (results != null) {

            new HashSet<>(results).forEach(scanResult -> {

                final BluetoothDevice device = scanResult.getDevice();

                final String key = getDeviceKey(device);

                Log.i(BL_DISCOVERY_HANDLER, "BLE Scan found: " + device.getName() + " - " + key);

                if (key != null) {
                    BluetoothDeviceTracer.trace(device, key, scanResult.getRssi(), BlType.BLE);
                } else {
                    Log.i(BL_DISCOVERY_HANDLER, "Device " + device.getName() + " has no tracing key. Ignored.");
                    BluetoothDeviceTracer.trace(device, key, scanResult.getRssi(), BlType.BLE);
                }
            });
        }
    }

    private String getDeviceKey(final BluetoothDevice device) {

        final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        };

        final BluetoothGatt gattClient = device.connectGatt(CovidTracingAndroidApp.getAppContext(), false, gattCallback);

        final BluetoothGattService service = gattClient.getService(BLE_KEY_EXCHANGE_SERVICE_UUID);

        if (service != null) {

            final BluetoothGattCharacteristic characteristic = service.getCharacteristic(BLE_KEY_EXCHANGE_CHARACTERISTIC_UUID);

            if (characteristic != null) {

                final String key = characteristic.getStringValue(0);
                gattClient.disconnect();
                gattClient.close();
                return key;
            } else {
                Log.d(BL_DISCOVERY_HANDLER, "Device " + device.getName() + " has no tracing characteristic active.");
            }
        } else {
            Log.d(BL_DISCOVERY_HANDLER, "Device " + device.getName() + " has no tracing service active.");
        }

        return null;
    }
}
