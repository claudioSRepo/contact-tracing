package it.cs.contact.tracing.contacts;

import android.util.Log;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.ConfirmedCase;
import it.cs.contact.tracing.model.entity.DeviceTrace;
import it.cs.contact.tracing.rest.connector.PositiveContactConnector;
import it.cs.contact.tracing.rest.connector.SecondLevelContactConnector;

public class ExposureAssessmentManager implements Runnable {

    private static final String TAG = "ExposureAssessmentManager";
    
    final PositiveContactConnector positiveContactConnector = new PositiveContactConnector();

    final SecondLevelContactConnector secondLevelContactConnector = new SecondLevelContactConnector();

    @Override
    public void run() {
        startPeriodicAssertion();
    }

    private void startPeriodicAssertion() {

        Log.i(TAG, "start Periodic risk Assertion.");
        
        // Retrieve my contacts with 20 day of depth
        final ConcurrentMap<String, List<DeviceTrace>> myContacts = retrieveMyContacts();
        
        Log.d(TAG, "My contacts retrieved: " +  myContacts.size());
        

        //Retrieve already confirmed cases
        final Map<String, ConfirmedCase> alreadyConfirmedCases = retrieveAlreadyConfirmedCases();

        Log.d(TAG, "Already confirmed cases: " +  alreadyConfirmedCases);
        
        final Set<String> deviceKeysToProcess = new HashSet<>(myContacts.keySet());
        deviceKeysToProcess.removeIf(alreadyConfirmedCases::containsKey);

        //For each contact, check if it has a positive swab.
        final Set<String> positiveContactsKeys =
                deviceKeysToProcess.stream().filter(this::checkIfPositiveSwab).collect(Collectors.toSet());


        if (!positiveContactsKeys.isEmpty()) {

            //At least one positive among contacts
            CovidTracingAndroidApp.getThreadPool().execute(new DirectContactExposure(myContacts, positiveContactsKeys));

        } else {

            // If there is no risk of direct contact with an infected person, the application checks if there have been second-level contacts

            final Set<String> secondLevelContactsKeys =
                    deviceKeysToProcess.stream().filter(this::checkSecondLevelContacts).collect(Collectors.toSet());

            if (!secondLevelContactsKeys.isEmpty()) {

                //At least one second level contacts
                CovidTracingAndroidApp.getThreadPool().execute(new SecondLevelContactExposure(myContacts, secondLevelContactsKeys));
            }
        }
    }


    private ConcurrentMap<String, List<DeviceTrace>> retrieveMyContacts() {

        final LocalDate fromDay = LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH);
        final List<DeviceTrace> myContacts = CovidTracingAndroidApp.getDb().deviceTraceDao().getAllContactsFromDay(fromDay);

        return myContacts.stream().collect(Collectors.groupingByConcurrent(DeviceTrace::getDeviceKey));
    }

    private Map<String, ConfirmedCase> retrieveAlreadyConfirmedCases() {

        final LocalDate fromDay = LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH);
        final List<ConfirmedCase> confirmedCases = CovidTracingAndroidApp.getDb().confirmedCaseDao().findBefore(fromDay);

        return confirmedCases.stream().collect(Collectors.toMap(ConfirmedCase::getDeviceKey, Function.identity()));
    }

    private boolean checkIfPositiveSwab(final String key) {

        return positiveContactConnector.verifyIfPositive(key, LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH));
    }

    private boolean checkSecondLevelContacts(final String key) {

        return secondLevelContactConnector.verifyIfExists(key, LocalDate.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH));
    }

}
