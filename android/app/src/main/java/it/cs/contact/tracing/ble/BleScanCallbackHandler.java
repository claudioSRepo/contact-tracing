package it.cs.contact.tracing.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.model.enums.BlType;
import it.cs.contact.tracing.tracing.BluetoothDeviceTracer;
import lombok.AllArgsConstructor;

import static it.cs.contact.tracing.config.InternalConfig.BLE_KEY_EXCHANGE_CHARACTERISTIC_UUID;
import static it.cs.contact.tracing.config.InternalConfig.BLE_KEY_EXCHANGE_SERVICE_UUID;

public class BleScanCallbackHandler extends ScanCallback {

    public static final String TAG = "BleScanCallbackHandler";

    private final Set<String> alreadyFound = new HashSet<>();

    @Override
    public void onScanResult(int callbackType, ScanResult scanResult) {

        if (scanResult != null) {

            final BluetoothDevice device = scanResult.getDevice();

            if (alreadyFound.contains(scanResult.getDevice().getAddress())) {
                Log.d(TAG, "BLE Scan ignoring : " + device.getAddress());
                return;
            }
            alreadyFound.add(scanResult.getDevice().getAddress());

            Log.i(TAG, "BLE Scan found: " + device.getName() + ". Gatt Connecting...");

            device.connectGatt(CovidTracingAndroidApp.getAppContext(), false, new BleGattClientCallbackHandler(scanResult));
        }

        super.onScanResult(callbackType, scanResult);
    }

    public void clear() {
        alreadyFound.clear();
    }

    @AllArgsConstructor
    final static class BleGattClientCallbackHandler extends BluetoothGattCallback {

        private final ScanResult scanResult;

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d(TAG, "Device " + gatt.getDevice().getName() + " state changed. Old status: " + status + ", newState: " + newState);

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {

                int bondstate = gatt.getDevice().getBondState();

                // Take action depending on the bond state
                if (bondstate == BluetoothDevice.BOND_NONE || bondstate == BluetoothDevice.BOND_BONDED) {

                    boolean result = gatt.discoverServices();

                    Log.d(TAG, "Discovery started: " + result);

                } else if (bondstate == BluetoothDevice.BOND_BONDING) {
                    // Bonding process in progress, let it complete
                    Log.d(TAG, "waiting for bonding to complete");
                }
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            super.onServicesDiscovered(gatt, status);

            final BluetoothDevice device = scanResult.getDevice();
            boolean disconnect = true;

            Log.d(TAG, "Device " + device.getName() + " service discovered. Status: " + status);

            try {

                final BluetoothGattService service = gatt.getService(BLE_KEY_EXCHANGE_SERVICE_UUID);

                if (service != null) {

                    final BluetoothGattCharacteristic characteristic = service.getCharacteristic(BLE_KEY_EXCHANGE_CHARACTERISTIC_UUID);

                    if (characteristic != null) {

                        Log.d(TAG, "Device " + device.getName() + " - reading characteristic...");
                        gatt.readCharacteristic(characteristic);
                        disconnect = false;

                    } else {
                        Log.d(TAG, "Device " + device.getName() + " has no tracing characteristic active. Ignored.");
                    }
                } else {
                    Log.d(TAG, "Device " + device.getName() + " has no tracing service active. Ignored.");
                }

            } catch (final Exception e) {

                Log.e(TAG, "Error while extracting key", e);
            } finally {

                if (disconnect) {
                    disconnect(gatt);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            try {
                final String characteristicValue = characteristic.getValue() != null ? new String(characteristic.getValue()) : null;

                if (characteristicValue != null) {

                    Log.i(TAG, "Device " + gatt.getDevice().getName() + " tracing key: " + characteristicValue);

                    BluetoothDeviceTracer.trace(gatt.getDevice(), characteristicValue, scanResult.getRssi(), BlType.BLE);
                } else {
                    Log.i(TAG, "Device " + gatt.getDevice().getName() + " has no tracing key. Ignored.");
                }

            } catch (final Exception e) {

                Log.e(TAG, "Error while extracting key", e);
            } finally {
                disconnect(gatt);
            }
        }

        private void disconnect(final BluetoothGatt gatt) {

            gatt.disconnect();
            gatt.close();
        }

    }
}
