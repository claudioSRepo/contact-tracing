package it.cs.contact.tracing.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import it.cs.contact.tracing.model.entity.Config;

@Dao
public interface ConfigDao {

    @Insert
    void insert(final Config config);

    @Query("SELECT * FROM Config WHERE `key` = :key")
    Config getConfigValue(final String key);
}
