package it.cs.contact.tracing.config.converters;

import androidx.room.TypeConverter;

import java.time.LocalDate;

public class DateConverter {

    @TypeConverter
    public static LocalDate fromTimestamp(final String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    @TypeConverter
    public static String dateToTimestamp(final LocalDate date) {
        return date == null ? null : date.toString();
    }
}
