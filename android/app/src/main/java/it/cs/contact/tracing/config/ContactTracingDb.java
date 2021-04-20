package it.cs.contact.tracing.config;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.cs.contact.tracing.config.converters.BlTypeConverter;
import it.cs.contact.tracing.config.converters.DateConverter;
import it.cs.contact.tracing.config.converters.NumberConverter;
import it.cs.contact.tracing.dao.DeviceTraceDao;
import it.cs.contact.tracing.model.entity.DeviceTrace;

@Database(entities = {DeviceTrace.class}, version = 4, exportSchema = false)
@TypeConverters({DateConverter.class, NumberConverter.class, BlTypeConverter.class})
public abstract class ContactTracingDb extends RoomDatabase {

    private static ContactTracingDb dbInstance = null;

    public abstract DeviceTraceDao deviceTraceDao();

    public static ContactTracingDb getInstance(final Context context) {

        if (dbInstance == null) {
            dbInstance = Room.databaseBuilder(context,
                    ContactTracingDb.class, "contact-tracing-db").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return dbInstance;
    }
}
