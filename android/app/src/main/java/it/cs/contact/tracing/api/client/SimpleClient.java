package it.cs.contact.tracing.api.client;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class SimpleClient {

    private static final String TAG = "SimpleClient";

    public static <T> void get(final String uri, final String resourceId, final ServiceResponseConverter responseConverter, final Consumer<T> responseCallBack) {

        new JsonObjectRequest
                (Request.Method.GET, addPathParam(uri, resourceId), null, new ResponseCallback<T>(responseConverter, responseCallBack),
                        error -> Log.e(TAG, "Error calling GET api " + addPathParam(uri, resourceId) + resourceId, error));
    }

    public static <T> void post(final String uri, final GenericServiceResource genericServiceResource, final ServiceRequestConverter requestConverter,
                                final ServiceResponseConverter responseConverter, final Consumer<T> responseCallBack) {

        new JsonObjectRequest
                (Request.Method.POST, uri, requestConverter.toJson(genericServiceResource), new ResponseCallback<>(responseConverter, responseCallBack),
                        error -> Log.e(TAG, "Error calling POST api " + uri, error));
    }

    public static <T> void put(final String uri, final GenericServiceResource genericServiceResource, final ServiceRequestConverter requestConverter,
                               final ServiceResponseConverter responseConverter, final Consumer<T> responseCallBack) {

        new JsonObjectRequest
                (Request.Method.PUT, addPathParam(uri, genericServiceResource.getResourceId()), requestConverter.toJson(genericServiceResource), new ResponseCallback<>(responseConverter, responseCallBack),
                        error -> Log.e(TAG, "Error calling PUT api " + addPathParam(uri, genericServiceResource.getResourceId()), error));
    }

    private static String addPathParam(final String uri, final String param) {

        return uri + "/" + param;
    }

    public interface GenericServiceResource {

        String getResourceId();
    }

    public interface ServiceResponseConverter {

        GenericServiceResource fromJson(final JSONObject response);
    }

    public interface ServiceRequestConverter {

        JSONObject toJson(final GenericServiceResource response);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @SuppressWarnings("unchecked")
    public static class ResponseCallback<T> implements Response.Listener<JSONObject> {

        private ServiceResponseConverter respConverter;
        private Consumer<T> responseCallBack;

        @Override
        public void onResponse(JSONObject response) {

            if (response != null) {

                responseCallBack.accept((T) respConverter.fromJson(response));
            }
        }
    }
}
