package it.cs.contact.tracing.contacts;

import android.util.Log;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.api.client.SimpleClient;
import it.cs.contact.tracing.api.dto.PositiveContactDTO;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.utils.ConTracUtils;

public class ExposureAssessmentManager implements Runnable {

    private static final String TAG = "ExposureAssessmentManager";

    @Override
    public void run() {
        startPeriodicAssertion();
    }

    private void startPeriodicAssertion() {

        Log.i(TAG, "start Periodic risk Assertion.");

        // Retrieve my contacts with 20 day of depth
        final ConcurrentMap<String, List<DeviceTrace>> myContacts = retrieveMyContacts();

        Log.d(TAG, "My contacts retrieved: " + myContacts.size());


        //Retrieve already confirmed cases
//        final Map<String, RiskEvalTracing> alreadyConfirmedCases = retrieveAlreadyConfirmedCases();

//        Log.d(TAG, "Already confirmed cases: " + alreadyConfirmedCases);

        final Set<String> deviceKeysToProcess = new HashSet<>(myContacts.keySet());
//        deviceKeysToProcess.removeIf(alreadyConfirmedCases::containsKey);

        //For each contact, check if it has a positive swab.
        deviceKeysToProcess.forEach(key -> processIfPositiveContact(key, myContacts));

        //For each contact, check if it has a second level contact
        deviceKeysToProcess.forEach(key -> processIfSecondLevelContact(key, myContacts));
    }


    private ConcurrentMap<String, List<DeviceTrace>> retrieveMyContacts() {

        final LocalDate fromDay = LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH);
        final List<DeviceTrace> myContacts = CovidTracingAndroidApp.getDb().deviceTraceDao().getAllContactsFromDay(fromDay);

        return myContacts.stream().collect(Collectors.groupingByConcurrent(DeviceTrace::getDeviceKey));
    }

//    private Map<String, RiskEvalTracing> retrieveAlreadyConfirmedCases() {
//
//        final LocalDate fromDay = LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH);
//        final List<RiskEvalTracing> confirmedCases = CovidTracingAndroidApp.getDb().confirmedCaseDao().findAfter(fromDay);
//
//        return confirmedCases.stream().collect(Collectors.toMap(RiskEvalTracing::getDeviceKey, Function.identity()));
//    }

    private void processIfPositiveContact(final String key, final ConcurrentMap<String, List<DeviceTrace>> myContacts) {

        SimpleClient.get(InternalConfig.POSITIVE_CONTACTS_URL, key, PositiveContactDTO::fromJson, (PositiveContactDTO dto) -> {

            Log.d(TAG, "Processing pc:  " + dto);

            if (dto != null && ConTracUtils.numberToDate(dto.getCommunicatedOn()).isAfter(LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH))) {

                Log.w(TAG, "Found positive contac to process: " + dto.getDeviceKey());

                new DirectContactExposure(myContacts, key).run();

                ConTracUtils.wait(10);
            }
        });

    }

    private void processIfSecondLevelContact(final String key, final ConcurrentMap<String, List<DeviceTrace>> myContacts) {

        SimpleClient.get(InternalConfig.SECOND_LEV_CONTACTS_URL, key, PositiveContactDTO::fromJson, (PositiveContactDTO dto) -> {

            Log.d(TAG, "Processing slc:  " + dto);

            if (dto != null && ConTracUtils.numberToDate(dto.getCommunicatedOn()).isAfter(LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH))) {

                Log.w(TAG, "Found second level contac to process: " + dto.getDeviceKey());

                new SecondLevelContactExposure(myContacts, key).run();
                ConTracUtils.wait(10);
            }
        });

    }

}
