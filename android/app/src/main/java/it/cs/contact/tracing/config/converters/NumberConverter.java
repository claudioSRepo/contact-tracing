package it.cs.contact.tracing.config.converters;

import androidx.room.TypeConverter;

import java.math.BigDecimal;

public class NumberConverter {

    @TypeConverter
    public static BigDecimal fromString(final String n) {
        return n == null ? null : new BigDecimal(n);
    }

    @TypeConverter
    public static String toString(final BigDecimal n) {
        return n == null ? null : n.toPlainString();
    }
}
