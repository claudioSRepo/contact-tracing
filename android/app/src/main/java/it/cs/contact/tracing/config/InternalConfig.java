package it.cs.contact.tracing.config;

import android.app.AlarmManager;

import java.util.UUID;

public interface InternalConfig {

    long BL_CHECKER_SCHEDULING_SEC = AlarmManager.INTERVAL_HOUR / 60 * 2;

    long BLE_SCAN_PERIOD = BL_CHECKER_SCHEDULING_SEC / 4;

//    Map<Range<Integer>, Risk.RiskZone> RISK_ZONE_MAP = Collections.unmodifiableMap(Stream.of(
//            entry(Range.between(0, 5), Risk.RiskZone.LOW),
//            entry(Range.between(6, 100), Risk.RiskZone.MEDIUM),
//            entry(Range.between(101, Integer.MAX_VALUE), Risk.RiskZone.HIGH)).collect(entriesToMap()));

    short DEFAULT_MIN_RSSI = -100;

    short MIN_EXPOSURE_TRACING = 1;

    UUID BLE_KEY_EXCHANGE_SERVICE_UUID = UUID.fromString("6bfc1913-25b1-469f-8de4-a94e3cbef3cd");

    UUID BLE_KEY_EXCHANGE_CHARACTERISTIC_UUID = UUID.fromString("778e0c53-2ceb-4c29-bdb0-38e22ff249ce");
}
