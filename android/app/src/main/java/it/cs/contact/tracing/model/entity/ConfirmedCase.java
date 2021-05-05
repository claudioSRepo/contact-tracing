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

@Data
@Builder
@Entity
@AllArgsConstructor
public class ConfirmedCase {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "device_key")
    private String deviceKey;

    @ColumnInfo(name = "reported_on")
    private LocalDate reportedOn;

    @ColumnInfo(name = "exposure")
    private BigDecimal exposure;
}
