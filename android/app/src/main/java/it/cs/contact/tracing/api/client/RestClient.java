package it.cs.contact.tracing.api.client;

import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.function.Consumer;

import it.cs.contact.tracing.CovidTracingAndroidApp;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

public class RestClient {

    private static final String TAG = "RestClient";

    private static RestClient instance = null;

    private RequestQueue requestQueue = null;

    private RestClient() {
    }

    public static RestClient getInstance() {

        if (instance == null) {
            instance = new RestClient();
            instance.requestQueue = Volley.newRequestQueue(CovidTracingAndroidApp.getAppContext());
        }

        return instance;
    }

    public <T> void get(final String uri, final String resourceId, final ServiceResponseConverter responseConverter, final Consumer<T> responseCallBack) {

        final CustomJsonRequest req = new CustomJsonRequest(Request.Method.GET, addPathParam(uri, resourceId), null, new ResponseCallback<T>(responseConverter, responseCallBack),
                error -> Log.e(TAG, "Error calling GET api " + addPathParam(uri, resourceId), error));

        req.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }

    public <T> void post(final String uri, final GenericServiceResource genericServiceResource, final ServiceRequestConverter requestConverter,
                         final ServiceResponseConverter responseConverter, final Consumer<T> responseCallBack) {

        final CustomJsonRequest req = new CustomJsonRequest(Request.Method.POST, uri, requestConverter.toJson(genericServiceResource), new ResponseCallback<T>(responseConverter, responseCallBack),
                error -> Log.e(TAG, "Error calling POST api " + uri, error));

        req.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }

    public <T> void put(final String uri, final GenericServiceResource genericServiceResource, final ServiceRequestConverter requestConverter,
                        final ServiceResponseConverter responseConverter, final Consumer<T> responseCallBack) {

        final CustomJsonRequest req = new CustomJsonRequest(Request.Method.PUT, addPathParam(uri, genericServiceResource.getResourceId()), requestConverter.toJson(genericServiceResource), new ResponseCallback<>(responseConverter, responseCallBack),
                error -> Log.e(TAG, "Error calling PUT api " + addPathParam(uri, genericServiceResource.getResourceId()), error));

        req.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }

    private String addPathParam(final String uri, final String param) {

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
        @SneakyThrows
        public void onResponse(final JSONObject response) {

            if (response != null) {

                responseCallBack.accept((T) respConverter.fromJson(response));
            }
        }
    }

    private static class CustomJsonRequest extends JsonObjectRequest {

        public CustomJsonRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
        }

        public CustomJsonRequest(String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
            super(url, jsonRequest, listener, errorListener);
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(final NetworkResponse response) {

            try {
                final String jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

                JSONObject result = null;

                if (StringUtils.trimToNull(jsonString) != null) {
                    result = new JSONObject(jsonString);
                }

                return Response.success(result,
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }
    }
}
