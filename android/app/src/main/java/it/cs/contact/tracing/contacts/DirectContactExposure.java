package it.cs.contact.tracing.contacts;

import android.util.Log;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.ConfirmedCase;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DirectContactExposure implements Runnable {

    public static final String TAG = "DirectContactExposure";

    final Map<String, List<DeviceTrace>> myContacts;

    final Set<String> positiveContactsKeys;

    @Override
    public void run() {

        Log.i(TAG, "Direct contact found!");

        final AtomicReference<BigDecimal> sum = new AtomicReference<>();
        sum.set(BigDecimal.ZERO);

        positiveContactsKeys.forEach(key -> {

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

                CovidTracingAndroidApp.getDb().confirmedCaseDao().insert(ConfirmedCase.builder().deviceKey(key).reportedOn(entry.getValue().getDate()).exposure(weightedRiskValue).build());

                sum.set(sum.get().add(weightedRiskValue));
            }
        });

        final BigDecimal totalRiskValue = sum.get();

        Log.i(TAG, "Total risk value:" + totalRiskValue);

        //TODO: Save into db total risk
    }

    private Integer getDay(final DeviceTrace t) {

        return (int) Duration.between(LocalDate.now().atStartOfDay(), t.getDate().atStartOfDay()).toDays();
    }
}
