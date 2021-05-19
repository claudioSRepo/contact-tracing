package it.cs.contact.tracing.api.dto;

import org.json.JSONObject;

import it.cs.contact.tracing.api.client.SimpleClient;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@Builder
public class PositiveContactDTO implements SimpleClient.GenericServiceResource {

    private String deviceKey;

    private int communicatedOn;

    @SneakyThrows
    public static SimpleClient.GenericServiceResource fromJson(final JSONObject jsonObject) {

        return builder().deviceKey(jsonObject.getString("deviceKey")).communicatedOn(jsonObject.getInt("communicatedOn")).build();
    }

    @SneakyThrows
    public static JSONObject toJson(final SimpleClient.GenericServiceResource res) {

        final JSONObject o = new JSONObject();
        o.put("deviceKey", ((PositiveContactDTO) res).deviceKey);
        o.put("communicatedOn", ((PositiveContactDTO) res).communicatedOn);
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
