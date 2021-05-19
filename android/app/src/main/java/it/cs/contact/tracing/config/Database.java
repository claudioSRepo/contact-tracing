package it.cs.contact.tracing.config;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.cs.contact.tracing.config.converters.BlTypeConverter;
import it.cs.contact.tracing.config.converters.ContactTypeConverter;
import it.cs.contact.tracing.config.converters.DateConverter;
import it.cs.contact.tracing.config.converters.NoiseConverter;
import it.cs.contact.tracing.config.converters.NumberConverter;
import it.cs.contact.tracing.config.converters.RiskZoneConverter;
import it.cs.contact.tracing.dao.ConfigDao;
import it.cs.contact.tracing.dao.CurrentRiskDao;
import it.cs.contact.tracing.dao.DeviceTraceDao;
import it.cs.contact.tracing.dao.RiskEvalTracingDao;
import it.cs.contact.tracing.model.entity.Config;
import it.cs.contact.tracing.model.entity.CurrentRisk;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.model.entity.RiskEvalTracing;

@androidx.room.Database(entities = {DeviceTrace.class, Config.class, RiskEvalTracing.class, CurrentRisk.class}, version = 12, exportSchema = false)
@TypeConverters({DateConverter.class, NumberConverter.class, BlTypeConverter.class, NoiseConverter.class, ContactTypeConverter.class, RiskZoneConverter.class})
public abstract class Database extends RoomDatabase {

    private static Database dbInstance = null;

    public abstract DeviceTraceDao deviceTraceDao();

    public abstract ConfigDao configDao();

    public abstract RiskEvalTracingDao riskEvalTracingDao();

    public abstract CurrentRiskDao currentRiskDao();

    public static synchronized Database getInstance(final Context context) {

        if (dbInstance == null) {
            dbInstance = Room.databaseBuilder(context,
                    Database.class, "contact-tracing-db").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        }
        return dbInstance;
    }
}
