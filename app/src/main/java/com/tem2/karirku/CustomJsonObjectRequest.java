package com.tem2.karirku;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class CustomJsonObjectRequest extends JsonObjectRequest {

    public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            // Handle empty response (common in Supabase with return=minimal)
            if (response.data.length == 0) {
                return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
            }

            String jsonString = new String(response.data, StandardCharsets.UTF_8);
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            // Jika parsing gagal, tetap return success dengan empty object
            return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
        }
    }
}