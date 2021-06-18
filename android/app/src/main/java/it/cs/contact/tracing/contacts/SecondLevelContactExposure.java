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
import it.cs.contact.tracing.model.enums.RiskType;
import it.cs.contact.tracing.model.enums.RiskZone;
import it.cs.contact.tracing.utils.ConTracUtils;
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

        if (currentRisk != null && currentRisk.getType().equals(RiskType.DIRECT_CONTACT)
                && currentRisk.getCalculatedOn().isAfter(ZonedDateTime.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH))) {

            //No action. Direct contact found.
            Log.d(TAG, "For device key " + key + ": NO ACTION. Direct Contact found " + currentRisk);
            return;
        }

        final AtomicReference<BigDecimal> totalRiskSum = new AtomicReference<>();
        totalRiskSum.set(BigDecimal.ZERO);

        final AtomicReference<BigDecimal> totalTimeSum = new AtomicReference<>();
        totalTimeSum.set(BigDecimal.ZERO);

        final List<DeviceTrace> myContactRecord = myContacts.get(key);

        assert myContactRecord != null;

        Log.d(TAG, "For device key " + key + "found " + myContactRecord.size() + " records.");

        final Map<Integer, DeviceTrace> grouped = myContactRecord.stream().collect(Collectors.toMap(this::getDay, Function.identity(), (date1, date2) -> {
            System.out.println("Duplicate key found for date " + date1);
            return date1;
        }));

        for (final Map.Entry<Integer, DeviceTrace> entry : grouped.entrySet()) {

            //Calculate risk for single day
            final BigDecimal weightedRiskValue = entry.getValue().getExposure().multiply(
                    InternalConfig.DISTRIBUTION_WEIGHT_MAP.getOrDefault(entry.getKey(), BigDecimal.ZERO)).multiply(InternalConfig.SECOND_LEVEL_CONTACT_FACTOR);

            Log.v(TAG, "Date: " + entry.getValue().getDate() + ", day: " + entry.getKey());
            Log.v(TAG, "Value: " + entry.getValue().getExposure() + ", weighted: " + weightedRiskValue);

            //Sum of risk value by day
            totalRiskSum.set(totalRiskSum.get().add(weightedRiskValue));

            //Sum of time spent with contact at risk
            totalTimeSum.set(totalRiskSum.get().add(entry.getValue().getExpositionTime()));

            //Save tracing to DB
            CovidTracingAndroidApp.getDb().riskEvalTracingDao().insert(RiskEvalTracing.builder()
                    .deviceKey(key)
                    .contactType(ContactType.SL)
                    .refDate(entry.getValue().getDate())
                    .dayExpositionTime(entry.getValue().getExpositionTime())
                    .dayExposure(weightedRiskValue)
                    .build());
        }

        final CurrentRisk riskEntity = CurrentRisk.builder()
                .deviceKey(key)
                .calculatedOn(ZonedDateTime.now())
                .totalExpositionTime(totalTimeSum.get())
                .totalRisk(totalRiskSum.get())
                .riskZone(evaluate(totalRiskSum.get()))
                .type(RiskType.SL_CONTACT).build();

        CovidTracingAndroidApp.getDb().currentRiskDao().insert(riskEntity);

        if (!riskEntity.getRiskZone().equals(RiskZone.LOW)) {
            ConTracUtils.sendNotification("E' stato rilevato un rischio di contagio. Entra nell'App.");
        }

        Log.i(TAG, "Total risk value:" + totalRiskSum.get());
    }

    private Integer getDay(final DeviceTrace t) {

        return (int) Duration.between(LocalDate.now().atStartOfDay(), t.getDate().atStartOfDay()).toDays();
    }

    private RiskZone evaluate(BigDecimal totalRiskValue) {

        return InternalConfig.RISK_ZONE_MAP.floorEntry(totalRiskValue.intValue()).getValue();
    }
}
