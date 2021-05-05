package it.cs.contact.tracing.config.converters;

import androidx.room.TypeConverter;

import it.cs.contact.tracing.audio.DecibelMeter;
import it.cs.contact.tracing.model.enums.BlType;

public class NoiseConverter {

    @TypeConverter
    public static DecibelMeter.Noise from(final String n) {
        return n == null ? null : DecibelMeter.Noise.valueOf(n);
    }

    @TypeConverter
    public static String to(final DecibelMeter.Noise n) {
        return n == null ? null : n.toString();
    }
}
