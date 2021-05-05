package it.cs.contact.tracing.tracing;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.audio.DecibelMeter;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.model.enums.BlType;
import it.cs.contact.tracing.utils.ConTracUtils;
import it.cs.contact.tracing.wifi.WifiUtils;

import static it.cs.contact.tracing.config.InternalConfig.BL_SCAN_SCHEDULING_OFFSET;
import static it.cs.contact.tracing.config.InternalConfig.MIDDLE_RSSI;
import static it.cs.contact.tracing.config.InternalConfig.MIN_EXPOSURE_TRACING;

public class BluetoothDeviceTracer {

    public static final String TAG = "BluetoothDeviceTracer";

    private static final double N = 2;

    public static void trace(final BluetoothDevice rawDevice, final String deviceKey, final int rssiSignalStrength, final BlType blType) {

        CovidTracingAndroidApp.getThreadPool().execute(() -> new BluetoothDeviceTracer().runAsyncTrace(rawDevice, deviceKey, rssiSignalStrength, blType));
        ConTracUtils.wait(1);
    }

    private void runAsyncTrace(final BluetoothDevice rawDevice, final String deviceKey, final int rssiSignalStrength, final BlType blType) {


        DeviceTrace deviceTrace = CovidTracingAndroidApp.getDb().deviceTraceDao().findByKey(deviceKey, blType);

        final BigDecimal estimatedDistance = getDistance(rssiSignalStrength);
        final boolean indoor = WifiUtils.isDeviceConnectedToWifi();
        final DecibelMeter.Noise noise = new DecibelMeter().recordAndgetNoiseValue();
        final BigDecimal exposure = getExposure(estimatedDistance, indoor, noise);

        if (exposure.compareTo(BigDecimal.valueOf(MIN_EXPOSURE_TRACING)) >= 0) {

            insert(toDeviceEntity(rssiSignalStrength, deviceKey, rawDevice, blType, estimatedDistance, exposure, indoor, noise));
//            if (deviceTrace != null) {
//
//                update(prepareUpdate(deviceTrace, rssiSignalStrength, estimatedDistance, exposure));
//
//            } else {
//                insert(toDeviceEntity(rssiSignalStrength, deviceKey, rawDevice, blType, hash, estimatedDistance, exposure));
//            }
        }
    }

    private DeviceTrace prepareUpdate(final DeviceTrace deviceTrace, final int rssiSignalStrength, final BigDecimal estimatedDistance, final BigDecimal exposure) {

        deviceTrace.setDistanceSum(deviceTrace.getDistanceSum().add(estimatedDistance));
        deviceTrace.setSignalStrengthSum(deviceTrace.getSignalStrengthSum() + rssiSignalStrength);
        deviceTrace.setExposure(deviceTrace.getExposure().add(exposure));
        deviceTrace.setUpdateVersion(deviceTrace.getUpdateVersion() + 1);
        deviceTrace.setTimestamp(ZonedDateTime.now());

        return deviceTrace;
    }

    private DeviceTrace toDeviceEntity(final int rssi, final String deviceKey, final BluetoothDevice deviceObj, final BlType blType,
                                       final BigDecimal estimatedDistance, final BigDecimal exposure, final boolean wifi, final DecibelMeter.Noise noise) {

        final String macAddress = deviceObj.getAddress();

        return DeviceTrace.builder()
                .deviceKey(deviceKey)
                .signalStrengthSum(rssi)
                .distanceSum(estimatedDistance)
                .updateVersion(1)
                .exposure(exposure)
                .from(blType)
                .noise(noise)
                .wifiConnected(wifi)
                .timestamp(ZonedDateTime.now())
                .date(LocalDate.now()).build();
    }


    private void insert(final DeviceTrace device) {

        Log.i(TAG, "New device trace saving for device: " + device.getDeviceKey());

        try {
            CovidTracingAndroidApp.getDb().deviceTraceDao().insert(device);

        } catch (final Exception e) {
            Log.e(TAG, "Error saving data for device" + device.getDeviceKey(), e);
        }

        Log.i(TAG, "Device trace added.");
    }

    private void update(final DeviceTrace device) {

        Log.i(TAG, "Update device trace for device: " + device.getDeviceKey());

        try {
            CovidTracingAndroidApp.getDb().deviceTraceDao().update(device);

        } catch (final Exception e) {
            Log.e(TAG, "Error updating data for device" + device.getDeviceKey(), e);
        }

        Log.i(TAG, "Device trace updated.");
    }

    private BigDecimal getDistance(final int rssi) {

        return BigDecimal.valueOf(Math.pow(10, (MIDDLE_RSSI - rssi) / (N * 10)));
    }

    private BigDecimal getExposure(final BigDecimal distance, final boolean deviceConnectedToWifi, final DecibelMeter.Noise noise) {

        final BigDecimal indoorMultiplier = deviceConnectedToWifi ? new BigDecimal("4") : BigDecimal.ONE;
        final BigDecimal noiseMultiplier = InternalConfig.NOISE_MULTIPLIER_MAP.get(noise);
        final BigDecimal adjustedDistance = distance.max(InternalConfig.MIN_DISTANCE);

        return BigDecimal.valueOf(BL_SCAN_SCHEDULING_OFFSET).
                divide(new BigDecimal("6000"), 1, RoundingMode.HALF_UP).
                divide(adjustedDistance, 1, RoundingMode.HALF_UP).
                multiply(indoorMultiplier).multiply(noiseMultiplier);
    }
}
