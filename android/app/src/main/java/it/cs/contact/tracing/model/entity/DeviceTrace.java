package it.cs.contact.tracing.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import it.cs.contact.tracing.audio.DecibelMeter;
import it.cs.contact.tracing.model.enums.BlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@Entity
@AllArgsConstructor
@ToString
public class DeviceTrace {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "device_key")
    private String deviceKey;

    @ColumnInfo(name = "signal_strength")
    private int signalStrength;

    @ColumnInfo(name = "distance")
    private BigDecimal distance;

    @ColumnInfo(name = "update_version")
    private int updateVersion;

    @ColumnInfo(name = "wifi_connected")
    private boolean wifiConnected;

    @ColumnInfo(name = "exposition_time")
    private BigDecimal expositionTime;

    @ColumnInfo(name = "exposure")
    private BigDecimal exposure;

    @ColumnInfo(name = "noise_value")
    private DecibelMeter.Noise noise;

    @ColumnInfo(name = "ref_date")
    private LocalDate date;

    @ColumnInfo(name = "timestamp")
    private ZonedDateTime timestamp;

    @ColumnInfo(name = "trace_from")
    private BlType from;
}
