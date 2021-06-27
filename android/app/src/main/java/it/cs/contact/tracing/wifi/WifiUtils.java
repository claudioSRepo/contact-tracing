package it.cs.contact.tracing.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import it.cs.contact.tracing.CovidTracingAndroidApp;

public class WifiUtils {

    public static final String TAG = "WifiUtils";

    public static boolean isDeviceConnectedToWifi() {

        try {
            final WifiManager wifiMgr = (WifiManager)
                    CovidTracingAndroidApp.getAppContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

                return wifiInfo.getNetworkId() != -1; // Not connected to an access point
            }
        } catch (final Exception e) {
            Log.e(TAG, "Error checking wifi", e);
        }

        return false;
    }
}
