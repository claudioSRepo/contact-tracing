package it.cs.contact.tracing.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
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
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.dao.ConfigDao;
import it.cs.contact.tracing.model.entity.Config;
import it.cs.contact.tracing.utils.ConTracUtils;

public class BleGattServer implements Runnable {

    private static final String TAG = "BleGattServer";

    private static final String TRACING_KEY_PARAM = "TRACING_KEY";

    private static BleGattServer bleGattServer;

    private final BluetoothManager mBluetoothManager;

    private final Context context;

    private final GattServerCallback mGattServerCallback = new GattServerCallback();

    private String tracingKey;

    private AtomicBoolean runnning = new AtomicBoolean(false);


    private BleGattServer(final BluetoothManager mBluetoothManager, final Context context) {
        this.mBluetoothManager = mBluetoothManager;
        this.context = context;
    }

    public static synchronized BleGattServer instanceOf(final BluetoothManager mBluetoothManager, final Context context) {

        if (bleGattServer == null) {
            bleGattServer = new BleGattServer(mBluetoothManager, context);
        }

        return bleGattServer;
    }

    @Override
    public void run() {

        try {
            if (!runnning.get()) {
                runnning.set(true);
                start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting server...", e);
        } finally {
            runnning.set(false);
        }
    }

    private void start() {

        Log.i(TAG, "start()");

        Log.i(TAG, "Shutdown old instances...");
        mGattServerCallback.shutdown();

        initTracingKey();

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
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        final AdvertiseData data = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(InternalConfig.BLE_ADVERTISE_TRACING_ACTIVE))
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build();

        mBluetoothLeAdvertiser
                .startAdvertising(settings, data, mAdvertiseCallback);
    }


    private void startServer() {

        Log.i(TAG, "Starting gatt server.");
        final BluetoothGattServer mBluetoothGattServer = mBluetoothManager.openGattServer(context, mGattServerCallback);

        mGattServerCallback.setGattServer(mBluetoothGattServer);

        if (mBluetoothGattServer == null) {
            Log.e(TAG, "Unable to create GATT server");
            return;
        }

        final BluetoothGattService service = new BluetoothGattService(InternalConfig.BLE_KEY_EXCHANGE_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);


        final BluetoothGattCharacteristic keyGattCharacteristic = new BluetoothGattCharacteristic(InternalConfig.BLE_KEY_EXCHANGE_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        keyGattCharacteristic.setValue(tracingKey);

        service.addCharacteristic(keyGattCharacteristic);

        mBluetoothGattServer.addService(service);
    }

    private void initTracingKey() {

        if (tracingKey == null) {

            final ConfigDao dao = CovidTracingAndroidApp.getDb().configDao();

            Config configEntity = dao.getConfigValue(TRACING_KEY_PARAM);

            if (configEntity == null) {

                configEntity = Config.builder().key(TRACING_KEY_PARAM).value(
                        ConTracUtils.secureHash(UUID.randomUUID().toString())).build();

                dao.insert(configEntity);
            }

            tracingKey = configEntity.getValue();
        }
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
            Log.i(TAG, "LE Advertise Failed: " + errorCode);
        }
    };

    private static class GattServerCallback extends BluetoothGattServerCallback {

        private BluetoothGattServer gattServer;

        public void setGattServer(final BluetoothGattServer gattServer) {
            this.gattServer = gattServer;
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {

            Log.v(TAG, " Connection state changed [" + status + "->" + newState + "] for device: " + device.getAddress() + " - " + device.getName());

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.v(TAG, "BluetoothDevice CONNECTED: " + device.getAddress() + " - " + device.getName());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                Log.v(TAG, "BluetoothDevice DISCONNECTED: " + device.getAddress() + " - " + device.getName());
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            Log.v(TAG, "Read Characteristic for device " + device.getAddress());
            Log.v(TAG, "Offset " + offset);
            Log.v(TAG, "Read Characteristic " + (characteristic.getValue() != null ? new String(characteristic.getValue()) : null));
            Log.v(TAG, "Read Characteristic " + characteristic.getStringValue(offset));

            final String fromOffset = StringUtils.trimToEmpty(characteristic.getStringValue(offset));

            gattServer.sendResponse(device, requestId,
                    BluetoothGatt.GATT_SUCCESS, offset, fromOffset.getBytes());
        }

        public void shutdown() {

            try {
                Log.v(TAG, "Shutting down...");
                gattServer.close();
                Log.v(TAG, "Waiting 5 seconds...");
                ConTracUtils.wait(5);

            } catch (final Exception ignored) {
            }
        }
    }
}
