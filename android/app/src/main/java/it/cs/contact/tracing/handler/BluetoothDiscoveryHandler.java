package it.cs.contact.tracing.handler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

import it.cs.contact.tracing.bl.BluetoothDeviceTracer;
import it.cs.contact.tracing.model.enums.BlType;

import static it.cs.contact.tracing.config.InternalConfig.DEFAULT_MIN_RSSI;

public class BluetoothDiscoveryHandler extends BroadcastReceiver {

    public static final String BL_DISCOVERY_HANDLER = "BlDiscoveryHandler";

    private final BluetoothAdapter blAdapter = BluetoothAdapter.getDefaultAdapter();

    final Map<String, Pair<Short, BluetoothDevice>> devices = new HashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            // Get the BluetoothDevice object from the Intent
            final BluetoothDevice latterDeviceObj = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            final String latterMacAddress = StringUtils.trimToEmpty(latterDeviceObj.getAddress());
            final short latterRssi = getRssi(intent);

            final Pair<Short, BluetoothDevice> former = devices.get(latterMacAddress);


            if (former == null || former.getLeft() < latterRssi) {

                devices.put(latterMacAddress, Pair.of(latterRssi, latterDeviceObj));
            }

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            Log.i(BL_DISCOVERY_HANDLER, "Discovery Finished : " + devices);
            blAdapter.cancelDiscovery();

            devices.values().forEach((d) -> BluetoothDeviceTracer.trace(d.getRight(), "NOKEY", d.getLeft(), BlType.BL));

        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

            devices.clear();
            Log.i(BL_DISCOVERY_HANDLER, "Discovery STARTED");
        }
    }

    private short getRssi(final Intent intent) {

        try {
            return intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

        } catch (final Exception e) {

            Log.e(BL_DISCOVERY_HANDLER, "RSSI NOT FOUND", e);
            return DEFAULT_MIN_RSSI;
        }
    }
}
