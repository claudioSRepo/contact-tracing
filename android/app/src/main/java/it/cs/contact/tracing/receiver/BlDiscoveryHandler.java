package it.cs.contact.tracing.receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static it.cs.contact.tracing.utils.ConTracUtils.secureHash;
import static it.cs.contact.tracing.utils.ConTracUtils.trimToEmpty;

public class BlDiscoveryHandler extends BroadcastReceiver {

    public static final String BL_DISCOVERY_HANDLER = "BlDiscoveryHandler";

    private final BluetoothAdapter blAdapter = BluetoothAdapter.getDefaultAdapter();

    final Set<BluetoothDevice> devices = new HashSet<>();

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            devices.add(device);

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            Log.i(BL_DISCOVERY_HANDLER, "Discovery Finished : " + devices);


            List<String> devicesKeys = devices.stream().map(this::getDeviceKey).collect(Collectors.toList());

            devicesKeys.forEach(d -> Log.d(BL_DISCOVERY_HANDLER, "Device Found: " + d));


            blAdapter.cancelDiscovery();

        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

            Log.i(BL_DISCOVERY_HANDLER, "Discovery STARTED");
        }
    }

    private String getDeviceKey(final BluetoothDevice device) {

        return device != null ? new StringBuilder(trimToEmpty(device.getName())).append("_").append(secureHash(device.getAddress())).toString() : "";
    }
}
