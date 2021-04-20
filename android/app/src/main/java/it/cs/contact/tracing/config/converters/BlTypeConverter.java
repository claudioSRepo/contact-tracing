package it.cs.contact.tracing.config.converters;

import androidx.room.TypeConverter;

import it.cs.contact.tracing.model.enums.BlType;

public class BlTypeConverter {

    @TypeConverter
    public static BlType from(final String n) {
        return n == null ? null : BlType.valueOf(n);
    }

    @TypeConverter
    public static String to(final BlType n) {
        return n == null ? null : n.toString();
    }
}
