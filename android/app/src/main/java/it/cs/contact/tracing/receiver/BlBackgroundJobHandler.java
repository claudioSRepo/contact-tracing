package it.cs.contact.tracing.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BlBackgroundJobHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.i("BlBackgroundJobHandler", "Started background process...");

        final BluetoothAdapter blAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.i("BlBackgroundJobHandler", "Started BL Adapter. Enabled: " + blAdapter.isEnabled());

        if (blAdapter.isEnabled()) {

            boolean started = blAdapter.startDiscovery();
            Log.i("BlBackgroundJobHandler", "Discovery started: " + started);
        }

    }
}
