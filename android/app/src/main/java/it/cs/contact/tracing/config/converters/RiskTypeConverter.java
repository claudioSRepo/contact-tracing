package it.cs.contact.tracing.config.converters;

import androidx.room.TypeConverter;

import it.cs.contact.tracing.model.enums.RiskType;

public class RiskTypeConverter {

    @TypeConverter
    public static RiskType from(final String n) {
        return n == null ? null : RiskType.valueOf(n);
    }

    @TypeConverter
    public static String to(final RiskType n) {
        return n == null ? null : n.toString();
    }
}
