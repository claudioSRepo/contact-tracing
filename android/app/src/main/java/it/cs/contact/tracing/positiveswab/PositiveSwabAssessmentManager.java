package it.cs.contact.tracing.positiveswab;

import android.util.Log;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.api.client.RestClient;
import it.cs.contact.tracing.api.dto.PositiveContactDTO;
import it.cs.contact.tracing.api.dto.SwabDTO;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.CurrentRisk;
import it.cs.contact.tracing.model.enums.RiskType;
import it.cs.contact.tracing.model.enums.RiskZone;
import it.cs.contact.tracing.utils.ConTracUtils;

public class PositiveSwabAssessmentManager implements Runnable {

    private static final String TAG = "PositiveSwabAssessmentManager";

    @Override
    public void run() {

        Log.d(TAG, "RUNNING()");
        startPeriodicAssertion();
    }

    private void startPeriodicAssertion() {

        Log.i(TAG, "start Periodic swab Assertion.");

        final String cf = CovidTracingAndroidApp.getDb().configDao().getConfigValue(InternalConfig.CF_PARAM).getValue();

        final CurrentRisk currentRisk = CovidTracingAndroidApp.getDb().currentRiskDao().findByKey(InternalConfig.MYSELF);
        Log.i(TAG, "Current risk" + currentRisk);

        if (currentRisk != null
                && currentRisk.getCalculatedOn().isAfter(
                ZonedDateTime
                        .now()
                        .minusDays(InternalConfig.POSITIVE_SWAB_VALIDITY_DAYS_LENGTH))
                && currentRisk.getRiskZone().equals(RiskZone.POSITIVE)) {

            Log.i(TAG, "Positive swab already present.");


        } else if (currentRisk != null
                && currentRisk.getCalculatedOn().isAfter(
                ZonedDateTime
                        .now()
                        .minusDays(InternalConfig.NEGATIVE_SWAB_VALIDITY_DAYS_LENGTH))
                && currentRisk.getRiskZone().equals(RiskZone.NEGATIVE)) {

            Log.i(TAG, "Negative swab already present.");

        } else {

            callSwabApiAndProcessResult(cf);
        }
    }

    private void callSwabApiAndProcessResult(final String cf) {

        RestClient.getInstance().get(InternalConfig.SWAB_MANAGEMENT_URL, cf, SwabDTO::fromJson, (SwabDTO dto) -> {

            Log.d(TAG, "Found positive swab:  " + dto);

            if (dto == null) {
                return;
            }

            if (ConTracUtils.numberToDate(dto.getReportedOn())
                    .isAfter(LocalDate.now().minusDays(InternalConfig.POSITIVE_SWAB_VALIDITY_DAYS_LENGTH))
                    && dto.getState().equals(SwabDTO.SwabState.POSITIVE)) {

                Log.i(TAG, "Current user has positive swab ");

                saveSwabState(dto.getReportedOn(), RiskZone.POSITIVE);
                signalPositivity();

                ConTracUtils.sendNotification("Il tuo tampone è positivo! Entra nell'App.");

            } else if (ConTracUtils.numberToDate(dto.getReportedOn())
                    .isAfter(LocalDate.now().minusDays(InternalConfig.NEGATIVE_SWAB_VALIDITY_DAYS_LENGTH))
                    && dto.getState().equals(SwabDTO.SwabState.NEGATIVE)) {

                Log.i(TAG, "Current user has negative swab ");
                saveSwabState(dto.getReportedOn(), RiskZone.NEGATIVE);

                ConTracUtils.sendNotification("Il tuo tampone è negativo! Entra nell'App.");
            }
        });
    }

    private void signalPositivity() {

        final String myDevKey = CovidTracingAndroidApp.getDb()
                .configDao().getConfigValue(InternalConfig.TRACING_KEY_PARAM).getValue();

        RestClient.getInstance()
                .get(InternalConfig.POSITIVE_CONTACTS_URL, myDevKey, PositiveContactDTO::fromJson,
                        (PositiveContactDTO prec) -> {

                            if (prec == null
                                    || ConTracUtils.numberToDate(prec.getCommunicatedOn())
                                    .isBefore(LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH))) {

                                final RestClient.GenericServiceResource dto =
                                        PositiveContactDTO.builder()
                                                .deviceKey(myDevKey)
                                                .communicatedOn(ConTracUtils.dateToNumber(LocalDate.now()))
                                                .build();

                                Log.d(TAG, "Sending device positivity:  " + dto);

                                RestClient.getInstance()
                                        .post(InternalConfig.POSITIVE_CONTACTS_URL, dto,
                                                PositiveContactDTO::toJson, PositiveContactDTO::fromJson,
                                                ConTracUtils::printSaved);
                            } else {
                                Log.d(TAG, "Positivity already present: " + prec);
                            }
                        });
    }

    private void saveSwabState(final Integer reportedOn, final RiskZone riskZone) {

        final CurrentRisk riskEntity = CurrentRisk.builder()
                .deviceKey(InternalConfig.MYSELF)
                .calculatedOn(ZonedDateTime.now())
                .swabbedOn(ConTracUtils.numberToDate(reportedOn))
                .totalExpositionTime(BigDecimal.ZERO)
                .totalRisk(BigDecimal.ZERO)
                .riskZone(riskZone)
                .type(RiskType.CONFIRMED_BY_SWAB).build();

        CovidTracingAndroidApp.getDb().currentRiskDao().insert(riskEntity);
    }
}
