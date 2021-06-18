package it.cs.contact.tracing.tracing;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

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
import static it.cs.contact.tracing.config.InternalConfig.N_ENV_FACTOR;

public class BluetoothDeviceTracer {

    public static final String TAG = "BluetoothDeviceTracer";

    private static final BigDecimal MINUTES_FROM_LAST_SCAN = BigDecimal.valueOf(BL_SCAN_SCHEDULING_OFFSET).
            divide(new BigDecimal("60000"), 1, RoundingMode.HALF_UP);

    public static void trace(final BluetoothDevice rawDevice, final String deviceKey, final int rssiSignalStrength, final BlType blType) {

        CovidTracingAndroidApp.getThreadPool().execute(
                () -> new BluetoothDeviceTracer().runAsyncTrace(rawDevice, deviceKey, rssiSignalStrength, blType));
        ConTracUtils.wait(1);
    }

    private void runAsyncTrace(final BluetoothDevice rawDevice, final String deviceKey, final int rssiSignalStrength, final BlType blType) {

        final BigDecimal estimatedDistance = getDistance(rssiSignalStrength);
        final boolean indoor = WifiUtils.isDeviceConnectedToWifi();
        final DecibelMeter.Noise noise = new DecibelMeter().recordAndgetNoiseValue();
        final BigDecimal exposure = getExposure(estimatedDistance, indoor, noise);

        if (exposure.compareTo(BigDecimal.valueOf(MIN_EXPOSURE_TRACING)) >= 0) {

            insert(toDeviceEntity(rssiSignalStrength, deviceKey, rawDevice, blType, estimatedDistance, exposure, indoor, noise));
        }
    }

    private DeviceTrace toDeviceEntity(final int rssi, final String deviceKey, final BluetoothDevice deviceObj, final BlType blType,
                                       final BigDecimal estimatedDistance, final BigDecimal exposure, final boolean wifi, final DecibelMeter.Noise noise) {

        return DeviceTrace.builder()
                .deviceKey(deviceKey)
                .signalStrength(rssi)
                .distance(estimatedDistance)
                .updateVersion(1)
                .exposure(exposure)
                .expositionTime(MINUTES_FROM_LAST_SCAN)
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

        final BigDecimal distance = BigDecimal.valueOf(Math.pow(10, (MIDDLE_RSSI - rssi) / (N_ENV_FACTOR * 10)));

        return distance.max(InternalConfig.MIN_DISTANCE).min(InternalConfig.MAX_DISTANCE);
    }

    private BigDecimal getExposure(final BigDecimal distance, final boolean deviceConnectedToWifi, final DecibelMeter.Noise noise) {

        final BigDecimal indoorMultiplier = deviceConnectedToWifi ? new BigDecimal("4") : BigDecimal.ONE;
        final BigDecimal noiseMultiplier = InternalConfig.NOISE_MULTIPLIER_MAP.get(noise);

        return MINUTES_FROM_LAST_SCAN
                .multiply(new BigDecimal("10"))
                .multiply(indoorMultiplier)
                .multiply(noiseMultiplier)
                .divide(distance, 1, RoundingMode.HALF_UP);
    }
}
