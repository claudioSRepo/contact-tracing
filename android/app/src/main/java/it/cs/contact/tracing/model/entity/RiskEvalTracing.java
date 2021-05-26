package it.cs.contact.tracing.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.time.LocalDate;

import it.cs.contact.tracing.model.enums.ContactType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@AllArgsConstructor
public class RiskEvalTracing {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "device_key")
    private String deviceKey;

    @ColumnInfo(name = "contact_type")
    private ContactType contactType;

    @ColumnInfo(name = "ref_date")
    private LocalDate refDate;

    @ColumnInfo(name = "day_exposure")
    private BigDecimal dayExposure;

    @ColumnInfo(name = "day_exposition_time")
    private BigDecimal dayExpositionTime;
}
