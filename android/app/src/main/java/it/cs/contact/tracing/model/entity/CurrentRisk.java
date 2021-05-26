package it.cs.contact.tracing.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import it.cs.contact.tracing.model.enums.RiskType;
import it.cs.contact.tracing.model.enums.RiskZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@AllArgsConstructor
public class CurrentRisk {

    @PrimaryKey()
    @ColumnInfo(name = "device_key")
    @androidx.annotation.NonNull
    private String deviceKey;

    @ColumnInfo(name = "type")
    private RiskType type;

    @ColumnInfo(name = "total_risk")
    private BigDecimal totalRisk;

    @ColumnInfo(name = "risk_zone")
    private RiskZone riskZone;

    @ColumnInfo(name = "total_exposition_time")
    private BigDecimal totalExpositionTime;

    @ColumnInfo(name = "calculated_on")
    private ZonedDateTime calculatedOn;

    @ColumnInfo(name = "swabbedOn")
    private LocalDate swabbedOn;
}
