package it.cs.contact.tracing.contacts;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RiskAssessment {

//    private final RiskDao dao;
//
//    public static RiskAssessment getInstance(final RiskDao dao) {
//        return new RiskAssessment(dao);
//    }
//
//    public Risk assess(final DeviceTrace trace) {
//
//        final Risk found = Optional.ofNullable(dao.findByHash(trace.getHash())).orElseGet(() -> createEmptyRisk(trace));
//
//
//
//        found.setCumulativeRiskValue(found.getCumulativeRiskValue().add(riskValue));
//        found.setRisk(assessZone(found.getCumulativeRiskValue()));
//    }
//
//    private Risk.RiskZone assessZone(BigDecimal cumulativeRiskValue) {
//
//        return InternalConfig.RISK_ZONE_MAP.entrySet().stream().
//                filter(e -> e.getKey().contains(cumulativeRiskValue.intValue()))
//                .findFirst().get().getValue();
//    }
//
//    private Risk createEmptyRisk(final DeviceTrace trace) {
//
//        return Risk.builder().deviceHash(trace.getHash()).cumulativeRiskValue(BigDecimal.ZERO).deviceName(StringUtils.trimToEmpty(trace.getName())).build();
//    }
}
