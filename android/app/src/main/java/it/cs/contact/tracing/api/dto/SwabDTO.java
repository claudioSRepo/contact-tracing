package it.cs.contact.tracing.api.dto;

import org.json.JSONObject;

import java.math.BigDecimal;

import it.cs.contact.tracing.api.client.SimpleClient;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

@Data
@Builder
public class SwabDTO implements SimpleClient.GenericServiceResource {

    @NonNull
    private String fiscalCode;

    private int reportedOn;

    private BigDecimal totalRisk;

    @NonNull
    private SwabState state;

    @SneakyThrows
    public static SimpleClient.GenericServiceResource fromJson(final JSONObject jsonObject) {

        return builder().fiscalCode(jsonObject.getString("fiscalCode")).reportedOn(jsonObject.getInt("reportedOn"))
                .totalRisk(new BigDecimal(jsonObject.getString("totalRisk")))
                .state(SwabState.valueOf(jsonObject.getString("state"))).build();
    }

    @SneakyThrows
    public static JSONObject toJson(final SimpleClient.GenericServiceResource res) {

        final JSONObject o = new JSONObject();
        o.put("fiscalCode", ((SwabDTO) res).fiscalCode);
        o.put("reportedOn", ((SwabDTO) res).reportedOn);
        o.put("totalRisk", ((SwabDTO) res).totalRisk);
        o.put("state", ((SwabDTO) res).state.toString());
        return o;
    }

    @Override
    public String getResourceId() {
        return fiscalCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SwabDTO{");
        sb.append("fiscalCode='").append(fiscalCode).append('\'');
        sb.append(", reportedOn=").append(reportedOn);
        sb.append(", totalRisk=").append(totalRisk);
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }

    public enum SwabState {

        IN_QUEUE, WAITING_FOR_RESULT, POSITIVE, NEGATIVE
    }
}
