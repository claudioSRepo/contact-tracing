package it.cs.contact.tracing.contacts;

import android.util.Log;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.CurrentRisk;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.model.entity.RiskEvalTracing;
import it.cs.contact.tracing.model.enums.ContactType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SecondLevelContactExposure implements Runnable {

    public static final String TAG = "SecondLevelContactExposure";

    final Map<String, List<DeviceTrace>> myContacts;

    final String key;

    @Override
    public void run() {

        Log.i(TAG, "Second level contact found!");

        Log.i(TAG, "Processing risk value");
        processRisk();
    }


    private void processRisk() {

        final CurrentRisk currentRisk = CovidTracingAndroidApp.getDb().currentRiskDao().findByKey(key);

        if (currentRisk != null && currentRisk.getType().equals(ContactType.DIRECT)
                && currentRisk.getCalculatedOn().isAfter(ZonedDateTime.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH))) {

            //No action. Direct contact found.
            Log.d(TAG, "For device key " + key + ": NO ACTION. Direct Contact found " + currentRisk);
            return;
        }

        final AtomicReference<BigDecimal> sum = new AtomicReference<>();
        sum.set(BigDecimal.ZERO);

        final List<DeviceTrace> myContactRecord = myContacts.get(key);

        assert myContactRecord != null;

        Log.d(TAG, "For device key " + key + "found " + myContactRecord.size() + " records.");

        final Map<Integer, DeviceTrace> grouped = myContactRecord.stream().collect(Collectors.toMap(this::getDay, Function.identity(), (date1, date2) -> {
            System.out.println("Duplicate key found for date " + date1);
            return date1;
        }));

        for (final Map.Entry<Integer, DeviceTrace> entry : grouped.entrySet()) {

            final BigDecimal weightedRiskValue = entry.getValue().getExposure().multiply(
                    InternalConfig.DISTRIBUTION_WEIGHT_MAP.getOrDefault(entry.getKey(), BigDecimal.ZERO)).multiply(InternalConfig.SECOND_LEVEL_CONTACT_FACTOR);

            Log.v(TAG, "Date: " + entry.getValue().getDate() + ", day: " + entry.getKey());
            Log.v(TAG, "Value: " + entry.getValue().getExposure() + ", weighted: " + weightedRiskValue);

            CovidTracingAndroidApp.getDb().riskEvalTracingDao().insert(RiskEvalTracing.builder().deviceKey(key)
                    .contactType(ContactType.SL).refDate(entry.getValue().getDate()).totalExposure(weightedRiskValue).build());

            sum.set(sum.get().add(weightedRiskValue));
        }

        final BigDecimal totalRiskValue = sum.get();
        final CurrentRisk riskEntity = CurrentRisk.builder().deviceKey(key).calculatedOn(ZonedDateTime.now()).totalRisk(totalRiskValue).riskZone(evaluate(totalRiskValue)).type(ContactType.DIRECT).build();


        CovidTracingAndroidApp.getDb().currentRiskDao().insert(riskEntity);

        Log.i(TAG, "Total risk value:" + totalRiskValue);
    }

    private Integer getDay(final DeviceTrace t) {

        return (int) Duration.between(LocalDate.now().atStartOfDay(), t.getDate().atStartOfDay()).toDays();
    }

    private String evaluate(BigDecimal totalRiskValue) {

        return InternalConfig.RISK_ZONE_MAP.floorKey(totalRiskValue.intValue()).toString();
    }
}
