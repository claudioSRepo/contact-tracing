package it.cs.contact.tracing.api.dto;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import it.cs.contact.tracing.api.client.RestClient;
import lombok.Builder;
import lombok.Data;

import static it.cs.contact.tracing.utils.ConTracUtils.getInt;
import static it.cs.contact.tracing.utils.ConTracUtils.getString;
import static it.cs.contact.tracing.utils.ConTracUtils.put;

@Data
@Builder
public class SwabDTO implements RestClient.GenericServiceResource {

    private final static String TAG = "SwabDTO";

    private String fiscalCode;

    private Integer reportedOn;

    private String totalRisk;

    private SwabState state;

    public static RestClient.GenericServiceResource fromJson(final JSONObject jsonObject) {

        Log.d(TAG, "Parsing json : " + jsonObject);

        if (jsonObject == null || getString(jsonObject, "fiscalCode") == null) return null;

        return builder().fiscalCode(getString(jsonObject, "fiscalCode"))
                .reportedOn(getInt(jsonObject, "reportedOn"))
                .totalRisk(getString(jsonObject, "totalRisk"))
                .state(SwabState.from(getString(jsonObject, "state")))
                .build();
    }

    public static JSONObject toJson(final RestClient.GenericServiceResource res) {

        if (res == null) return null;

        final JSONObject o = new JSONObject();
        put(o, "fiscalCode", ((SwabDTO) res).fiscalCode);
        put(o, "reportedOn", ((SwabDTO) res).reportedOn);
        put(o, "totalRisk", ((SwabDTO) res).totalRisk);
        put(o, "state", ((SwabDTO) res).state);
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

        IN_QUEUE, WAITING_FOR_RESULT, POSITIVE, NEGATIVE;

        private final static Set<String> values = new HashSet<>(SwabState.values().length);

        static {
            for (final SwabState f : SwabState.values())
                values.add(f.name());
        }

        public static SwabState from(final String key) {
            return values.contains(key) ? SwabState.valueOf(key) : null;
        }
    }
}
