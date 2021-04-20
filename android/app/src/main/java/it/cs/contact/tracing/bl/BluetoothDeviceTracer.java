package it.cs.contact.tracing.bl;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.ContactTracingDb;
import it.cs.contact.tracing.dao.DeviceTraceDao;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.model.enums.BlType;
import it.cs.contact.tracing.utils.ConTracUtils;

import static it.cs.contact.tracing.config.InternalConfig.BL_CHECKER_SCHEDULING_SEC;
import static it.cs.contact.tracing.config.InternalConfig.MIN_EXPOSURE_TRACING;

public class BluetoothDeviceTracer {

    public static final String TAG = "BluetoothDeviceTracer";

    private static final double R0 = -65;
    private static final double N = 2;

    private final static DeviceTraceDao tracingDao = ContactTracingDb.getInstance(CovidTracingAndroidApp.getAppContext()).deviceTraceDao();

    public static void trace(final BluetoothDevice rawDevice, final String deviceKey, final int rssiSignalStrength, final BlType blType) {

        CovidTracingAndroidApp.getThreadPool().execute(() -> new BluetoothDeviceTracer().runAsyncTrace(rawDevice, deviceKey, rssiSignalStrength, blType));
    }

    private void runAsyncTrace(final BluetoothDevice rawDevice, final String deviceKey, final int rssiSignalStrength, final BlType blType) {

        final String hash = ConTracUtils.secureHash(rawDevice.getAddress());
        DeviceTrace deviceTrace = tracingDao.findByHash(hash, blType);

        final BigDecimal estimatedDistance = getDistance(rssiSignalStrength);
        final BigDecimal exposure = getExposure(estimatedDistance);

        if (exposure.compareTo(BigDecimal.valueOf(MIN_EXPOSURE_TRACING)) >= 0) {
            insert(toDeviceEntity(rssiSignalStrength, deviceKey, rawDevice, blType, hash, estimatedDistance, exposure));
//            if (deviceTrace != null) {
//TODO: aggiungere update
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

        return deviceTrace;
    }

    private DeviceTrace toDeviceEntity(final int rssi, final String deviceKey, final BluetoothDevice deviceObj, final BlType blType, final String hash,
                                       final BigDecimal estimatedDistance, final BigDecimal exposure) {

        final String macAddress = deviceObj.getAddress();

        return DeviceTrace.builder()
                .name(StringUtils.trimToEmpty(deviceObj.getName()))
                .hash(hash)
                .deviceKey(deviceKey)
                .signalStrengthSum(rssi)
                .distanceSum(estimatedDistance)
                .updateVersion(1)
                .mac(macAddress)
                .exposure(exposure)
                .from(blType)
                .date(LocalDate.now()).build();
    }


    private void insert(final DeviceTrace device) {

        Log.i(TAG, "New device trace saving for device: " + device.getName());

        try {
            tracingDao.insert(device);

        } catch (final Exception e) {
            Log.e(TAG, "Error saving data for device" + device.getName(), e);
        }

        Log.i(TAG, "Device trace added.");
    }

    private void update(final DeviceTrace device) {

        Log.i(TAG, "Update device trace for device: " + device.getName());

        try {
            tracingDao.update(device);

        } catch (final Exception e) {
            Log.e(TAG, "Error updating data for device" + device.getName(), e);
        }

        Log.i(TAG, "Device trace updated.");
    }

    private BigDecimal getDistance(final int rssi) {

        return BigDecimal.valueOf(Math.pow(10, (R0 - rssi) / (N * 10)));
    }

    private BigDecimal getExposure(final BigDecimal distance) {

        return BigDecimal.valueOf(BL_CHECKER_SCHEDULING_SEC).
                divide(new BigDecimal("6000"), 1, RoundingMode.HALF_UP).
                divide(distance, 1, RoundingMode.HALF_UP).
                multiply(BigDecimal.ONE);
    }
}
