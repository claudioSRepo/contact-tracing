package it.cs.contact.tracing.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.pm.PackageManager;
import android.util.Log;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.InternalConfig;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GattServer {

    private static final String TAG = "GattServer";

    /* Bluetooth API */
    private final BluetoothManager mBluetoothManager;

    public static GattServer of(final BluetoothManager mBluetoothManager) {

        final GattServer server = new GattServer(mBluetoothManager);
        return server;
    }

    private final GattServerCallback mGattServerCallback = new GattServerCallback();

    public void start() {

        Log.i(TAG, "onCreate");

        final BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();

        // We can't continue without proper Bluetooth support
        if (checkBluetoothSupport(bluetoothAdapter)) {
            if (!bluetoothAdapter.isEnabled()) {
                Log.i(TAG, "Bluetooth is currently disabled.");
            } else {

                Log.i(TAG, "Bluetooth enabled...starting services");
                startServer();
                startAdvertising();
            }
        }
    }


    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {

        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!CovidTracingAndroidApp.getAppContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }

        return true;
    }


    /**
     * Begin advertising over Bluetooth that this device is connectable
     * and supports the Current Time Service.
     */
    private void startAdvertising() {

        final BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        final BluetoothLeAdvertiser mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        if (mBluetoothLeAdvertiser == null) {
            Log.w(TAG, "Failed to create advertiser");
            return;
        }

        final AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(false)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        final AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .build();

        mBluetoothLeAdvertiser
                .startAdvertising(settings, data, mAdvertiseCallback);
    }


    private void startServer() {

        Log.i(TAG, "Starting gatt server.");
        final BluetoothGattServer mBluetoothGattServer = mBluetoothManager.openGattServer(CovidTracingAndroidApp.getAppContext(), mGattServerCallback);
        mGattServerCallback.setGattServer(mBluetoothGattServer);


        if (mBluetoothGattServer == null) {
            Log.e(TAG, "Unable to create GATT server");
            return;
        }

        final BluetoothGattService service = new BluetoothGattService(InternalConfig.BLE_KEY_EXCHANGE_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);


        final BluetoothGattCharacteristic currentTime = new BluetoothGattCharacteristic(InternalConfig.BLE_KEY_EXCHANGE_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        currentTime.setValue("prova1234");

        service.addCharacteristic(currentTime);

        mBluetoothGattServer.addService(service);
    }

    /**
     * Callback to receive information about the advertisement process.
     */
    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: " + errorCode);
        }
    };

    private static class GattServerCallback extends BluetoothGattServerCallback {

        private BluetoothGattServer gattServer;

        public void setGattServer(final BluetoothGattServer gattServer) {
            this.gattServer = gattServer;
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i(TAG, "BluetoothDevice CONNECTED: " + device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                Log.i(TAG, "BluetoothDevice DISCONNECTED: " + device);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            gattServer.sendResponse(device, requestId, 1, offset, characteristic.getValue());
        }
    }
}
