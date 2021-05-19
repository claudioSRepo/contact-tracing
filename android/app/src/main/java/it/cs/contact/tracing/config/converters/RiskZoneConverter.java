package it.cs.contact.tracing.config.converters;

import androidx.room.TypeConverter;

import it.cs.contact.tracing.model.enums.RiskZone;

public class RiskZoneConverter {

    @TypeConverter
    public static RiskZone from(final String n) {
        return n == null ? null : RiskZone.valueOf(n);
    }

    @TypeConverter
    public static String to(final RiskZone n) {
        return n == null ? null : n.toString();
    }
}
