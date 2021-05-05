package it.cs.contact.tracing.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.model.enums.BlType;

@Dao
public interface DeviceTraceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final DeviceTrace trace);

    @Query("SELECT * FROM DeviceTrace WHERE device_key = :key AND date('now') = date(ref_date) AND trace_from = :blType LIMIT 1")
    DeviceTrace findByKey(final String key, final BlType blType);

    @Query("SELECT * FROM DeviceTrace WHERE date(ref_date) >= :day")
    List<DeviceTrace> getAllContactsFromDay(final LocalDate day);

    @Update
    void update(final DeviceTrace trace);
}
