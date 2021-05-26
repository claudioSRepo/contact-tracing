package it.cs.contact.tracing.api.dto;

import org.json.JSONObject;

import it.cs.contact.tracing.api.client.RestClient;
import lombok.Builder;
import lombok.Data;

import static it.cs.contact.tracing.utils.ConTracUtils.getInt;
import static it.cs.contact.tracing.utils.ConTracUtils.getString;
import static it.cs.contact.tracing.utils.ConTracUtils.put;

@Data
@Builder
public class SecondLevelContactDTO implements RestClient.GenericServiceResource {

    private String deviceKey;

    private Integer communicatedOn;

    public static RestClient.GenericServiceResource fromJson(final JSONObject jsonObject) {

        if (jsonObject == null || getString(jsonObject, "deviceKey") == null) return null;

        return builder().deviceKey(getString(jsonObject, "deviceKey"))
                .communicatedOn(getInt(jsonObject, "communicatedOn"))
                .build();
    }

    public static JSONObject toJson(final RestClient.GenericServiceResource res) {

        if (res == null) return null;

        final JSONObject o = new JSONObject();

        put(o, "deviceKey", ((SecondLevelContactDTO) res).deviceKey);
        put(o, "communicatedOn", ((SecondLevelContactDTO) res).communicatedOn);
        return o;
    }

    @Override
    public String getResourceId() {
        return deviceKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PositiveContactDTO{");
        sb.append("deviceKey='").append(deviceKey).append('\'');
        sb.append(", communicatedOn=").append(communicatedOn);
        sb.append('}');
        return sb.toString();
    }
}
