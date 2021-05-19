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
import it.cs.contact.tracing.api.client.SimpleClient;
import it.cs.contact.tracing.api.dto.SecondLevelContactDTO;
import it.cs.contact.tracing.api.dto.SwabDTO;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.CurrentRisk;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.model.entity.RiskEvalTracing;
import it.cs.contact.tracing.model.enums.ContactType;
import it.cs.contact.tracing.utils.ConTracUtils;
import lombok.AllArgsConstructor;

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
                    InternalConfig.DISTRIBUTION_WEIGHT_MAP.getOrDefault(entry.getKey(), BigDecimal.ZERO));

            Log.v(TAG, "Date: " + entry.getValue().getDate() + ", day: " + entry.getKey());
            Log.v(TAG, "Value: " + entry.getValue().getExposure() + ", weighted: " + weightedRiskValue);

            CovidTracingAndroidApp.getDb().riskEvalTracingDao().insert(RiskEvalTracing.builder().deviceKey(key)
                    .contactType(ContactType.DIRECT).refDate(entry.getValue().getDate()).totalExposure(weightedRiskValue).build());

            sum.set(sum.get().add(weightedRiskValue));
        }

        final BigDecimal totalRiskValue = sum.get();
        final CurrentRisk riskEntity = CurrentRisk.builder().deviceKey(key).calculatedOn(ZonedDateTime.now()).totalRisk(totalRiskValue).riskZone(evaluate(totalRiskValue)).type(ContactType.DIRECT).build();

        CovidTracingAndroidApp.getDb().currentRiskDao().insert(riskEntity);

        Log.i(TAG, "Total risk value:" + totalRiskValue);
        return totalRiskValue;
    }


    private void addToSwabQueue(final BigDecimal totalRiskValue) {

        final String cf = CovidTracingAndroidApp.getDb().configDao().getConfigValue(InternalConfig.CF_PARAM).getValue();
        final int dateNumber = ConTracUtils.dateToNumber(LocalDate.now());

        final SimpleClient.GenericServiceResource dto = SwabDTO.builder().fiscalCode(cf).reportedOn(dateNumber).totalRisk(totalRiskValue).state(SwabDTO.SwabState.IN_QUEUE).build();

        SimpleClient.put(InternalConfig.SWAB_MANAGEMENT_URL, dto, SwabDTO::toJson, SwabDTO::fromJson, (swabDTO) -> {
        });
    }

    private void registerSecondLevelContact() {

        final String deviceKey = CovidTracingAndroidApp.getDb().configDao().getConfigValue(TRACING_KEY_PARAM).getValue();
        final int dateNumber = ConTracUtils.dateToNumber(LocalDate.now());

        final SimpleClient.GenericServiceResource dto = SecondLevelContactDTO.builder().deviceKey(deviceKey).communicatedOn(dateNumber).build();

        SimpleClient.post(InternalConfig.SECOND_LEV_CONTACTS_URL, dto, SecondLevelContactDTO::toJson, SecondLevelContactDTO::fromJson, (SecondLevelContactDTO) -> {
        });
    }

    private Integer getDay(final DeviceTrace t) {

        return (int) Duration.between(LocalDate.now().atStartOfDay(), t.getDate().atStartOfDay()).toDays();
    }

    private String evaluate(BigDecimal totalRiskValue) {

        return InternalConfig.RISK_ZONE_MAP.floorKey(totalRiskValue.intValue()).toString();
    }
}
