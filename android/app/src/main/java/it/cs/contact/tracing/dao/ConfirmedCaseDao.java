package it.cs.contact.tracing.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.time.LocalDate;
import java.util.List;

import it.cs.contact.tracing.model.entity.ConfirmedCase;

@Dao
public interface ConfirmedCaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final ConfirmedCase confirmedCase);

    @Query("SELECT * FROM ConfirmedCase WHERE date(reported_on) = :day")
    List<ConfirmedCase> findBefore(final LocalDate day);
}
