package it.cs.contact.tracing.config.converters;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class DateConverter {

    @TypeConverter
    public static LocalDate toDate(final String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    @TypeConverter
    public static String dateToString(final LocalDate date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static ZonedDateTime toTimestamp(final String value) {
        return value == null ? null : ZonedDateTime.parse(value);
    }

    @TypeConverter
    public static String tsToString(final ZonedDateTime date) {
        return date == null ? null : date.toString();
    }
}
