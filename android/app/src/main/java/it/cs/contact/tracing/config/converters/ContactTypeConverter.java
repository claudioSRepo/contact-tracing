package it.cs.contact.tracing.config.converters;

import androidx.room.TypeConverter;

import it.cs.contact.tracing.model.enums.ContactType;

public class ContactTypeConverter {

    @TypeConverter
    public static ContactType from(final String n) {
        return n == null ? null : ContactType.valueOf(n);
    }

    @TypeConverter
    public static String to(final ContactType n) {
        return n == null ? null : n.toString();
    }
}
