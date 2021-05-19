package it.cs.contact.tracing.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import it.cs.contact.tracing.model.entity.CurrentRisk;

@Dao
public interface CurrentRiskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final CurrentRisk r);

    @Query("SELECT * FROM CurrentRisk WHERE device_key = :deviceKey LIMIT 1")
    CurrentRisk findByKey(final String deviceKey);

    @Update
    void update(final CurrentRisk trace);
}
