package it.cs.contact.tracing.positiveswab;

import android.util.Log;

import java.time.LocalDate;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.api.client.SimpleClient;
import it.cs.contact.tracing.api.dto.PositiveContactDTO;
import it.cs.contact.tracing.api.dto.SwabDTO;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.utils.ConTracUtils;

public class PositiveSwabAssessmentManager implements Runnable {

    private static final String TAG = "PositiveSwabAssessmentManager";

    @Override
    public void run() {
        startPeriodicAssertion();
    }

    private void startPeriodicAssertion() {

        Log.i(TAG, "start Periodic positive swab Assertion.");
        processIfPositiveSwab();
    }

    private void processIfPositiveSwab() {

        final String cf = CovidTracingAndroidApp.getDb().configDao().getConfigValue(InternalConfig.CF_PARAM).getValue();

        SimpleClient.get(InternalConfig.SWAB_MANAGEMENT_URL, cf, SwabDTO::fromJson, (SwabDTO dto) -> {

            Log.d(TAG, "Found positive swab:  " + dto);

            if (dto != null && ConTracUtils.numberToDate(dto.getReportedOn()).isAfter(LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH))
                    && dto.getState().equals(SwabDTO.SwabState.POSITIVE)
            ) {

                Log.i(TAG, "Current user have positive swab ");

                signalPositivity();
            }
        });

    }

    private void signalPositivity() {

        final String myKey = CovidTracingAndroidApp.getDb().configDao().getConfigValue(InternalConfig.TRACING_KEY_PARAM).getValue();

        final SimpleClient.GenericServiceResource dto = PositiveContactDTO.builder().deviceKey(myKey).communicatedOn(ConTracUtils.dateToNumber(LocalDate.now())).build();

        SimpleClient.post(InternalConfig.POSITIVE_CONTACTS_URL, dto, PositiveContactDTO::toJson, PositiveContactDTO::fromJson, (PositiveContactDTO) -> {
        });
    }
}
