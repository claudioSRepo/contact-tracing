package it.cs.contact.tracing.flutter;

import android.util.Log;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import io.flutter.plugin.common.MethodChannel;
import it.cs.contact.tracing.CovidTracingAndroidApp;
import it.cs.contact.tracing.config.InternalConfig;
import it.cs.contact.tracing.model.entity.CurrentRisk;
import it.cs.contact.tracing.model.enums.RiskType;
import it.cs.contact.tracing.model.enums.RiskZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static it.cs.contact.tracing.utils.ConTracUtils.put;
import static it.cs.contact.tracing.utils.ConTracUtils.sendNotification;

public class UiRiskProvider {

    private static final String TAG = "UiRiskProvider";

    private static final String PATTERN_HOUR = "HH:mm";

    public static final MethodChannel.MethodCallHandler getRiskSummaryHandler = (call, result) -> {

        Log.d(TAG, "Method channel, called:  " + call.method);

        switch (call.method) {

            case "getSummaryRisk":
                result.success(UiRiskProvider.getRiskDetail().getRiskZoneCode());
                break;
            case "getRiskDetail":
                result.success(UiRiskProvider.getRiskDetail().toJson());
                break;
            default:
                result.notImplemented();
                break;
        }
    };

    private static UiRiskDto getRiskDetail() {

        //CHECK IF SWABBED (POSITIVE/NEGATIVE)
        final CurrentRisk currentRisk = CovidTracingAndroidApp.getDb().currentRiskDao().findByKey(InternalConfig.MYSELF);

        Log.i(TAG, "Current risk" + currentRisk);

        if (currentRisk != null && currentRisk.getCalculatedOn().isAfter(ZonedDateTime.now().minusDays(InternalConfig.POSITIVE_SWAB_VALIDITY_DAYS_LENGTH))
                && currentRisk.getRiskZone().equals(RiskZone.POSITIVE)) {

            Log.d(TAG, "Create positive risk result.");
            return createSwabResult(currentRisk);

        } else if (currentRisk != null && currentRisk.getCalculatedOn().isAfter(ZonedDateTime.now().minusDays(InternalConfig.NEGATIVE_SWAB_VALIDITY_DAYS_LENGTH))
                && currentRisk.getRiskZone().equals(RiskZone.NEGATIVE)) {

            Log.d(TAG, "Create negative risk result.");
            return createSwabResult(currentRisk);
        }

        //CHECK TRACING ...
        final List<CurrentRisk> risks = CovidTracingAndroidApp.getDb().currentRiskDao().findFrom(ZonedDateTime.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH));

        if (risks != null && !risks.isEmpty()) {

            Log.d(TAG, "Create standard risk result.");

            final BigDecimal[] riskValueSum = {BigDecimal.ZERO};
            final BigDecimal[] timeSpentWithPC = {BigDecimal.ZERO};
            final BigDecimal[] timeSpentWithSLC = {BigDecimal.ZERO};
            final LongAdder numberOfContacts = new LongAdder();
            final ZonedDateTime[] calculatedOn = {ZonedDateTime.now().minusDays(InternalConfig.TRACING_DAYS_LENGTH)};

            final Map<RiskType, List<CurrentRisk>> collectedByType =
                    risks.stream().collect(Collectors.groupingBy(CurrentRisk::getType));

            collectedByType.getOrDefault(RiskType.DIRECT_CONTACT, Collections.emptyList()).forEach(risk -> {

                riskValueSum[0] = riskValueSum[0].add(risk.getTotalRisk());
                timeSpentWithPC[0] = timeSpentWithPC[0].add(risk.getTotalExpositionTime());
                calculatedOn[0] = calculatedOn[0].isAfter(risk.getCalculatedOn()) ? calculatedOn[0] : risk.getCalculatedOn();
                numberOfContacts.add(1);
            });

            collectedByType.getOrDefault(RiskType.SL_CONTACT, Collections.emptyList()).forEach(risk -> {

                riskValueSum[0] = riskValueSum[0].add(risk.getTotalRisk());
                timeSpentWithSLC[0] = timeSpentWithSLC[0].add(risk.getTotalExpositionTime());
                calculatedOn[0] = calculatedOn[0].isAfter(risk.getCalculatedOn()) ? calculatedOn[0] : risk.getCalculatedOn();
                numberOfContacts.add(1);
            });

            return createRiskResult(riskValueSum[0], timeSpentWithPC[0], timeSpentWithSLC[0],
                    numberOfContacts.longValue(), calculatedOn[0]);
        }

        Log.d(TAG, "No current risk found.");
        sendNotification("test");
        return UiRiskDto.builder()
                .calculatedOnDate(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDateTime.now()))
                .calculatedOnTime(DateTimeFormatter.ofPattern(PATTERN_HOUR).format(LocalDateTime.now()))
                .build();

        //MIN
//        return createRiskResult(new BigDecimal("100"), new BigDecimal("0"), new BigDecimal("0"),
//                0, ZonedDateTime.now());

        //MED
//        return createRiskResult(new BigDecimal("400"), new BigDecimal("30"), new BigDecimal("30"),
//                2, ZonedDateTime.now());

        //MAX
//        return createRiskResult(new BigDecimal("1001"), new BigDecimal("45"), new BigDecimal("45"),
//                2, ZonedDateTime.now());


        //NEGATIVO
//         return createSwabResult(CurrentRisk.builder().deviceKey("het")
//                .totalRisk(BigDecimal.ZERO).calculatedOn(ZonedDateTime.now()).swabbedOn(LocalDate.now()).riskZone(RiskZone.NEGATIVE).build());


        //POSITIVO
//        return createSwabResult(CurrentRisk.builder().deviceKey("het")
//                .totalRisk(BigDecimal.ZERO).calculatedOn(ZonedDateTime.now()).swabbedOn(LocalDate.now()).riskZone(RiskZone.POSITIVE).build());
    }

    private static UiRiskDto createRiskResult(final BigDecimal riskValueSum, final BigDecimal timeSpentWithPC,
                                              final BigDecimal timeSpentWithSLC, final long numberOfContacts,
                                              final ZonedDateTime calculatedOn) {

        return UiRiskDto.builder()
                .riskValue(riskValueSum.max(new BigDecimal("1")).min(new BigDecimal("1000"))
                        .setScale(0, RoundingMode.HALF_UP).toPlainString() + "\n/1000")
                .riskZoneCode(evaluateRiskZoneCode(riskValueSum))
                .riskZoneDesc(evaluateRiskZoneDesc(riskValueSum))
                .riskZoneThumbUp(evaluateRiskZoneThumbUp(riskValueSum))
                .riskPercentage(evaluatePercentage(riskValueSum))
                .timeSpentWithPC(timeSpentWithPC.setScale(0, RoundingMode.HALF_UP).toPlainString())
                .timeSpentWithPCThumbUp(timeSpentWithPC.setScale(0, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) == 0)
                .timeSpentWithSLC(timeSpentWithSLC.setScale(0, RoundingMode.HALF_UP).toPlainString())
                .timeSpentWithSLCThumbUp(timeSpentWithSLC.compareTo(BigDecimal.ZERO) == 0)
                .numberOfContacts(String.valueOf(numberOfContacts))
                .numberOfContactsThumbUp(numberOfContacts == 0)
                .calculatedOnDate(DateTimeFormatter.ISO_LOCAL_DATE.format(calculatedOn))
                .calculatedOnTime(DateTimeFormatter.ofPattern(PATTERN_HOUR).format(calculatedOn))
                .build();
    }

    private static UiRiskDto createSwabResult(final CurrentRisk currentRisk) {

        return UiRiskDto.builder()
                .riskZoneCode(currentRisk.getRiskZone().toString())
                .riskZoneDesc(currentRisk.getRiskZone().toIta())
                .riskZoneThumbUp(!currentRisk.getRiskZone().equals(RiskZone.POSITIVE))
                .riskPercentage(currentRisk.getRiskZone().equals(RiskZone.POSITIVE) ? 96 : 1)
                .riskValue(currentRisk.getRiskZone().equals(RiskZone.POSITIVE) ? "MAX" : "1\n/1000")
                .timeSpentWithPCThumbUp(!currentRisk.getRiskZone().equals(RiskZone.POSITIVE))
                .timeSpentWithSLCThumbUp(!currentRisk.getRiskZone().equals(RiskZone.POSITIVE))
                .numberOfContactsThumbUp(!currentRisk.getRiskZone().equals(RiskZone.POSITIVE))
                .calculatedOnDate(DateTimeFormatter.ISO_LOCAL_DATE.format(currentRisk.getCalculatedOn()))
                .calculatedOnTime(DateTimeFormatter.ofPattern(PATTERN_HOUR).format(currentRisk.getCalculatedOn()))
                .swabDoneOnDate(DateTimeFormatter.ISO_LOCAL_DATE.format(currentRisk.getSwabbedOn()))
                .build();
    }

    private static int evaluatePercentage(final BigDecimal riskValueSum) {

        double val = riskValueSum.doubleValue() / (double) InternalConfig.RISK_MAX_VAL;
        int percentage = val < 0.96 ? (int) (val * 100) : 96;

        Log.d(TAG, "From " + riskValueSum + " , percentage: " + percentage);
        return percentage;
    }

    private static String evaluateRiskZoneCode(final BigDecimal riskValueSum) {

        return evaluateRiskZone(riskValueSum).toString();
    }

    private static String evaluateRiskZoneDesc(final BigDecimal riskValueSum) {

        return evaluateRiskZone(riskValueSum).toIta();
    }

    private static RiskZone evaluateRiskZone(final BigDecimal riskValueSum) {

        return InternalConfig.RISK_ZONE_MAP.floorEntry(riskValueSum.intValue()).getValue();
    }

    private static boolean evaluateRiskZoneThumbUp(final BigDecimal riskValueSum) {

        return evaluateRiskZoneCode(riskValueSum).equalsIgnoreCase(RiskZone.LOW.toString());
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    static class UiRiskDto {

        @Builder.Default
        private final String riskValue = "1/1000";

        @Builder.Default
        private final int riskPercentage = 1;

        @Builder.Default
        private final String riskZoneCode = "LOW";

        @Builder.Default
        private final String riskZoneDesc = RiskZone.LOW.toIta();

        @Builder.Default
        private final String timeSpentWithPC = "0";

        @Builder.Default
        private final String timeSpentWithSLC = "0";

        @Builder.Default
        private final String numberOfContacts = "0";

        @Builder.Default
        private final String calculatedOnDate = "ND";

        @Builder.Default
        private final String calculatedOnTime = "ND";

        @Builder.Default
        private final String swabDoneOnDate = "ND";

        @Builder.Default
        private final boolean riskValueThumbUp = true;

        @Builder.Default
        private final boolean riskPercentageOThumbUp = true;

        @Builder.Default
        private final boolean riskZoneThumbUp = true;

        @Builder.Default
        private final boolean timeSpentWithPCThumbUp = true;

        @Builder.Default
        private final boolean timeSpentWithSLCThumbUp = true;

        @Builder.Default
        private final boolean numberOfContactsThumbUp = true;

        public String toJson() {

            final JSONObject o = new JSONObject();

            put(o, "riskValue", riskValue);
            put(o, "riskPercentage", riskPercentage);
            put(o, "riskZoneCode", riskZoneCode);
            put(o, "riskZoneDesc", riskZoneDesc);
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

            return o.toString();
        }
    }
}
