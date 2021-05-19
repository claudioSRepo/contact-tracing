package it.cs.contact.tracing.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.time.LocalDate;
import java.util.List;

import it.cs.contact.tracing.model.entity.RiskEvalTracing;

@Dao
public interface RiskEvalTracingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final RiskEvalTracing riskEvalTracing);

    @Query("SELECT * FROM RiskEvalTracing WHERE date(ref_date) >= :day")
    List<RiskEvalTracing> findAfter(final LocalDate day);
}
