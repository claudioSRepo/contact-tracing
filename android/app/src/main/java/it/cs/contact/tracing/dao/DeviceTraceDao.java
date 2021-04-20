package it.cs.contact.tracing.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.model.enums.BlType;

@Dao
public interface DeviceTraceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final DeviceTrace trace);

    @Query("SELECT * FROM DeviceTrace WHERE device_hash = :hash AND date('now') = date(ref_date) AND trace_from = :blType LIMIT 1")
    DeviceTrace findByHash(final String hash, final BlType blType);

    @Update
    void update(final DeviceTrace trace);
}
