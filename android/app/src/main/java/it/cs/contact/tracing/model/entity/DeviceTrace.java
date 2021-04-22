package it.cs.contact.tracing.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import it.cs.contact.tracing.model.enums.BlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@AllArgsConstructor
public class DeviceTrace {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "device_mac")
    private String mac;

    @ColumnInfo(name = "device_hash")
    private String hash;

    @ColumnInfo(name = "device_key")
    private String deviceKey;

    @ColumnInfo(name = "device_name")
    private String name;

    @ColumnInfo(name = "signal_strength_sum")
    private int signalStrengthSum;

    @ColumnInfo(name = "distance_sum")
    private BigDecimal distanceSum;

    @ColumnInfo(name = "update_version")
    private int updateVersion;

    @ColumnInfo(name = "exposure")
    private BigDecimal exposure;

    @ColumnInfo(name = "ref_date")
    private LocalDate date;

    @ColumnInfo(name = "timestamp")
    private ZonedDateTime timestamp;

    @ColumnInfo(name = "trace_from")
    private BlType from;
}
