package it.cs.contact.tracing.config;

import android.app.AlarmManager;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;
import java.util.UUID;
import java.util.stream.Stream;

import it.cs.contact.tracing.audio.DecibelMeter;
import it.cs.contact.tracing.utils.MapUtils;

import static it.cs.contact.tracing.utils.MapUtils.entriesToMap;
import static it.cs.contact.tracing.utils.MapUtils.entry;

public interface InternalConfig {

    //Scanning
    long BL_FIRST_SCAN = 5000;
    long BL_SCAN_SCHEDULING_OFFSET = AlarmManager.INTERVAL_HOUR / 60 * 2;
    long BLE_SCAN_PERIOD = BL_SCAN_SCHEDULING_OFFSET / 4;
    long BLE_RESTART_SERVER = 3600000 / 4;
    long EXPOSURE_ASSESSMENT_SCHEDULING = AlarmManager.INTERVAL_HOUR;

//    Map<Range<Integer>, Risk.RiskZone> RISK_ZONE_MAP = Collections.unmodifiableMap(Stream.of(
//            entry(Range.between(0, 5), Risk.RiskZone.LOW),
//            entry(Range.between(6, 100), Risk.RiskZone.MEDIUM),
//            entry(Range.between(101, Integer.MAX_VALUE), Risk.RiskZone.HIGH)).collect(entriesToMap()));

    //Distance calc
    double MIDDLE_RSSI = -65;
    BigDecimal MIN_DISTANCE = new BigDecimal("2");

    //Exchange Keys
    UUID BLE_ADVERTISE_TRACING_ACTIVE = UUID.fromString("fb290d48-77a2-4324-ba16-d337e1d7c2b8");
    UUID BLE_KEY_EXCHANGE_SERVICE_UUID = UUID.fromString("6bfc1913-25b1-469f-8de4-a94e3cbef3cd");
    UUID BLE_KEY_EXCHANGE_CHARACTERISTIC_UUID = UUID.fromString("778e0c53-2ceb-4c29-bdb0-38e22ff249ce");

    //Tracing
    short TRACING_DAYS_LENGTH = 14;
    short MIN_EXPOSURE_TRACING = 0;

    Map<Integer, BigDecimal> DISTRIBUTION_WEIGHT_MAP = Stream.of(
            entry(0, BigDecimal.valueOf(1)),
            entry(1, BigDecimal.valueOf(1)),
            entry(2, BigDecimal.valueOf(0.98)),
            entry(3, BigDecimal.valueOf(0.96)),
            entry(4, BigDecimal.valueOf(0.92)),
            entry(5, BigDecimal.valueOf(0.88)),
            entry(6, BigDecimal.valueOf(0.84)),
            entry(7, BigDecimal.valueOf(0.78)),
            entry(8, BigDecimal.valueOf(0.73)),
            entry(9, BigDecimal.valueOf(0.67)),
            entry(10, BigDecimal.valueOf(0.61)),
            entry(11, BigDecimal.valueOf(0.55)),
            entry(12, BigDecimal.valueOf(0.49)),
            entry(13, BigDecimal.valueOf(0.43)),
            entry(14, BigDecimal.valueOf(0.38)),
            entry(15, BigDecimal.valueOf(0.32)),
            entry(16, BigDecimal.valueOf(0.28)),
            entry(17, BigDecimal.valueOf(0.24)),
            entry(18, BigDecimal.valueOf(0.2)),
            entry(19, BigDecimal.valueOf(0.16)),
            entry(20, BigDecimal.valueOf(0.14))).collect(entriesToMap()); //Gaussian distr.


    //Noise configuration
    NavigableMap<Integer, DecibelMeter.Noise> NOISE_ZONE_MAP = MapUtils.toNavigableMap(Stream.of(
            entry(0, DecibelMeter.Noise.LOW),
            entry(60, DecibelMeter.Noise.MEDIUM),
            entry(80, DecibelMeter.Noise.HIGH)).collect(entriesToMap()));

    Map<DecibelMeter.Noise, BigDecimal> NOISE_MULTIPLIER_MAP = Stream.of(
            entry(DecibelMeter.Noise.LOW, BigDecimal.ONE),
            entry(DecibelMeter.Noise.MEDIUM, new BigDecimal("2")),
            entry(DecibelMeter.Noise.HIGH, new BigDecimal("4"))).collect(entriesToMap());
}
