package it.cs.contact.tracing.flutter;

import android.util.Log;

import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import io.flutter.plugin.common.MethodChannel;
import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.api.client.RestClient;
import it.cs.contact.tracing.api.dto.PositiveContactDTO;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.CurrentRisk;
import it.cs.contact.tracing.model.enums.RiskZone;
import lombok.NoArgsConstructor;

import static it.cs.contact.tracing.utils.ConTracUtils.put;

public class UiRiskProvider {

    private static final String TAG = "UiRiskProvider";
    
    private static UiRiskDto getRiskDetail() {

        //CHECK IF SWABBED (POSITIVE/NEGATIVE)
        final CurrentRisk currentRisk = CovidTracingAndroidApp.getDb().currentRiskDao().findByKey(InternalConfig.MYSELF);

        Log.i(TAG, "Current risk" + currentRisk);

        if (currentRisk != null && currentRisk.getCalculatedOn().isAfter(ZonedDateTime.now().minusDays(InternalConfig.POSITIVE_SWAB_VALIDITY_DAYS_LENGTH))
                && currentRisk.getRiskZone().equals(RiskZone.POSITIVE)) {

            return createPositiveResult(currentRisk);


        } else if (currentRisk != null && currentRisk.getCalculatedOn().isAfter(ZonedDateTime.now().minusDays(InternalConfig.NEGATIVE_SWAB_VALIDITY_DAYS_LENGTH))
                && currentRisk.getRiskZone().equals(RiskZone.NEGATIVE)) {

            return createNegativeResult(currentRisk);
        }

        //CHECK TRACING ...

        final List<CurrentRisk> risks = CovidTracingAndroidApp.getDb().currentRiskDao().findFrom(ZonedDateTime.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH));
        
        if(risks != null ) {

            //If positive
            Optional<CurrentRisk> foundPositive = risks.stream().filter(UiRiskProvider::checkIfOnePositive).findFirst();
            
            if(foundPositive.isPresent()) {

            }
            
            //If not positive
            
        }
        
        
        return new UiRiskDto().toJson();
    }

    private static UiRiskDto createNegativeResult(CurrentRisk currentRisk) {
    }

    private static UiRiskDto createPositiveResult(final CurrentRisk currentRisk) {
    }

    public static final MethodChannel.MethodCallHandler getRiskSummaryHandler = (call, result) -> {

        Log.d(TAG, "Method channel, called:  " + call.method);

        switch (call.method) {

            case "getSummaryRisk":
                result.success(UiRiskProvider.getSummaryRisk());
                break;
            case "getRiskDetail":
                result.success(UiRiskProvider.getRiskDetail());
                break;
            default:
                result.notImplemented();
                break;
        }
    };

    @NoArgsConstructor
    static class UiRiskDto {

        private String riskValue = "35";

        private int riskPercentage = 70;

        private String riskZone = "ALTA";

        private String timeSpentWithPC = "20";

        private String timeSpentWithSLC = "21";

        private String numberOfContacts = "12";

        private String calculatedOnDate = "31/99/9999";

        private String calculatedOnTime = "12:38";

        private String swabDoneOnDate = "30/99/9999";

        private boolean riskValueThumbUp = true;

        private boolean riskPercentageOThumbUp = true;

        private boolean riskZoneThumbUp = true;

        private boolean timeSpentWithPCThumbUp = false;

        private boolean timeSpentWithSLCThumbUp = false;

        private boolean numberOfContactsThumbUp = false;

        private boolean calculatedOnDateThumbUp = false;

        private boolean calculatedOnTimeThumbUp = false;

        private boolean swabDoneOnDateThumbUp = true;

        public String toJson() {

            final JSONObject o = new JSONObject();

            put(o, "riskValue", riskValue);
            put(o, "riskPercentage", riskPercentage);
            put(o, "riskZone", riskZone);
            put(o, "timeSpentWithPC", timeSpentWithPC);
            put(o, "timeSpentWithSLC", timeSpentWithSLC);
            put(o, "numberOfContacts", numberOfContacts);
            put(o, "calculatedOnDate", calculatedOnDate);
            put(o, "calculatedOnTime", calculatedOnTime);
            put(o, "swabDoneOnDate", swabDoneOnDate);

            put(o, "riskValueThumbUp", riskValueThumbUp);
            put(o, "riskPercentageOThumbUp", riskPercentageOThumbUp);
            put(o, "riskZoneThumbUp", riskZoneThumbUp);
            put(o, "timeSpentWithPCThumbUp", timeSpentWithPCThumbUp);
            put(o, "timeSpentWithSLCThumbUp", timeSpentWithSLCThumbUp);
            put(o, "numberOfContactsThumbUp", numberOfContactsThumbUp);
            put(o, "calculatedOnDateThumbUp", calculatedOnDateThumbUp);
            put(o, "calculatedOnTimeThumbUp", calculatedOnTimeThumbUp);
            put(o, "swabDoneOnDateThumbUp", swabDoneOnDateThumbUp);

            return o.toString();
        }
    }
}
