package it.cs.contact.tracing.config;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.cs.contact.tracing.config.converters.BlTypeConverter;
import it.cs.contact.tracing.config.converters.DateConverter;
import it.cs.contact.tracing.config.converters.NoiseConverter;
import it.cs.contact.tracing.config.converters.NumberConverter;
import it.cs.contact.tracing.dao.ConfigDao;
import it.cs.contact.tracing.dao.ConfirmedCaseDao;
import it.cs.contact.tracing.dao.DeviceTraceDao;
import it.cs.contact.tracing.model.entity.Config;
import it.cs.contact.tracing.model.entity.ConfirmedCase;
import it.cs.contact.tracing.model.entity.DeviceTrace;

@androidx.room.Database(entities = {DeviceTrace.class, Config.class, ConfirmedCase.class}, version = 11, exportSchema = false)
@TypeConverters({DateConverter.class, NumberConverter.class, BlTypeConverter.class, NoiseConverter.class})
public abstract class Database extends RoomDatabase {

    private static Database dbInstance = null;

    public abstract DeviceTraceDao deviceTraceDao();

    public abstract ConfigDao configDao();

    public abstract ConfirmedCaseDao confirmedCaseDao();

    public static synchronized Database getInstance(final Context context) {

        if (dbInstance == null) {
            dbInstance = Room.databaseBuilder(context,
                    Database.class, "contact-tracing-db").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return dbInstance;
    }
}
