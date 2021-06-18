package it.cs.contact.tracing.contacts;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.api.client.RestClient;
import it.cs.contact.tracing.api.dto.SecondLevelContactDTO;
import it.cs.contact.tracing.api.dto.SwabDTO;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.exception.CTProcessExeption;
import it.cs.contact.tracing.model.entity.CurrentRisk;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.model.entity.RiskEvalTracing;
import it.cs.contact.tracing.model.enums.ContactType;
import it.cs.contact.tracing.model.enums.RiskType;
import it.cs.contact.tracing.model.enums.RiskZone;
import it.cs.contact.tracing.utils.ConTracUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import static it.cs.contact.tracing.config.InternalConfig.TRACING_KEY_PARAM;

@AllArgsConstructor
public class DirectContactExposure implements Runnable {

    public static final String TAG = "DirectContactExposure";

    final Map<String, List<DeviceTrace>> myContacts;

    final String key;

    @Override
    public void run() {

        Log.i(TAG, "Direct contact found!");

        Log.i(TAG, "Processing risk value");
        final BigDecimal totalRiskValue = processRisk();

        Log.i(TAG, "Inserting in swab queue");
        addToSwabQueue(totalRiskValue);

        Log.i(TAG, "Inserting in second level contact");
        registerSecondLevelContact();
    }

    private BigDecimal processRisk() {

        final AtomicReference<BigDecimal> totalRiskSum = new AtomicReference<>();
        totalRiskSum.set(BigDecimal.ZERO);

        final AtomicReference<BigDecimal> totalTimeSum = new AtomicReference<>();
        totalTimeSum.set(BigDecimal.ZERO);

        final List<DeviceTrace> myContactRecord = myContacts.get(key);

        assert myContactRecord != null;

        Log.d(TAG, "For device key " + key + "found " + myContactRecord.size() + " records.");

        final Map<Integer, DeviceTrace> grouped = myContactRecord.stream().collect(Collectors.toMap(this::getDay, Function.identity(), this::add));

        for (final Map.Entry<Integer, DeviceTrace> entry : grouped.entrySet()) {

            //Calculate risk for single day
            final BigDecimal weightedRiskValue = entry.getValue().getExposure().multiply(
                    InternalConfig.DISTRIBUTION_WEIGHT_MAP.getOrDefault(entry.getKey(), BigDecimal.ZERO));

            Log.v(TAG, "Date: " + entry.getValue().getDate() + ", day: " + entry.getKey());
            Log.v(TAG, "Value: " + entry.getValue().getExposure() + ", weighted: " + weightedRiskValue);

            //Sum of risk value by day
            totalRiskSum.set(totalRiskSum.get().add(weightedRiskValue));

            //Sum of time spent with contact at risk
            totalTimeSum.set(totalTimeSum.get().add(entry.getValue().getExpositionTime()));

            //Save tracing to DB
            CovidTracingAndroidApp.getDb().riskEvalTracingDao().insert(RiskEvalTracing.builder()
                    .deviceKey(key)
                    .contactType(ContactType.DIRECT)
                    .refDate(entry.getValue().getDate())
                    .dayExpositionTime(entry.getValue().getExpositionTime())
                    .dayExposure(weightedRiskValue)
                    .build());

        }

        //Save risk
        final CurrentRisk riskEntity = CurrentRisk.builder()
                .deviceKey(key)
                .calculatedOn(ZonedDateTime.now())
                .totalExpositionTime(totalTimeSum.get())
                .totalRisk(totalRiskSum.get())
                .riskZone(evaluate(totalRiskSum.get()))
                .type(RiskType.DIRECT_CONTACT).build();

        CovidTracingAndroidApp.getDb().currentRiskDao().insert(riskEntity);

        if (!riskEntity.getRiskZone().equals(RiskZone.LOW)) {
            ConTracUtils.sendNotification("E' stato rilevato un rischio di contagio. Entra nell'App.");
        }

        Log.i(TAG, "Total risk value:" + totalRiskSum.get());
        return totalRiskSum.get();
    }


    private void addToSwabQueue(final BigDecimal totalRiskValue) {

        final String cf = CovidTracingAndroidApp.getDb().configDao().getConfigValue(InternalConfig.CF_PARAM).getValue();
        final int dateNumber = ConTracUtils.dateToNumber(LocalDate.now());

        final RestClient.GenericServiceResource dto = SwabDTO.builder().fiscalCode(cf)
                .reportedOn(dateNumber).totalRisk(totalRiskValue.toPlainString()).state(SwabDTO.SwabState.IN_QUEUE).build();

        RestClient.getInstance().put(InternalConfig.SWAB_MANAGEMENT_URL, dto, SwabDTO::toJson, SwabDTO::fromJson, ConTracUtils::printSaved);
    }

    private void registerSecondLevelContact() {

        final String deviceKey = CovidTracingAndroidApp.getDb().configDao().getConfigValue(TRACING_KEY_PARAM).getValue();
        final int dateNumber = ConTracUtils.dateToNumber(LocalDate.now());

        final RestClient.GenericServiceResource dto = SecondLevelContactDTO.builder().deviceKey(deviceKey).communicatedOn(dateNumber).build();

        RestClient.getInstance().post(InternalConfig.SECOND_LEV_CONTACTS_URL, dto, SecondLevelContactDTO::toJson, SecondLevelContactDTO::fromJson, ConTracUtils::printSaved);
    }

    private Integer getDay(final DeviceTrace t) {

        return (int) Duration.between(LocalDate.now().atStartOfDay(), t.getDate().atStartOfDay()).toDays();
    }

    private RiskZone evaluate(BigDecimal totalRiskValue) {

        return InternalConfig.RISK_ZONE_MAP.floorEntry(totalRiskValue.intValue()).getValue();
    }

    @SneakyThrows
    private DeviceTrace add(final DeviceTrace d1, final DeviceTrace d2) {

        if (!d1.getDate().equals(d2.getDate()) || !d1.getDeviceKey().equals(d2.getDeviceKey())) {
            throw new CTProcessExeption();
        }

        d1.setDistance(d1.getDistance().add(d2.getDistance()));
        d1.setSignalStrength(d1.getSignalStrength() + d2.getSignalStrength());
        d1.setExposure(d1.getExposure().add(d2.getExposure()));
        d1.setExpositionTime(d1.getExpositionTime().add(d2.getExpositionTime()));
        d1.setUpdateVersion(d1.getUpdateVersion() + d2.getUpdateVersion());
        d1.setTimestamp(d1.getTimestamp());

        return d1;
    }
}
