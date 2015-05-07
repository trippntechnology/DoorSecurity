package com.trippntechnology.doorsecurity;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public class KeyReturnRequest extends Request<KeyReturn> {

    private final Gson gson = new Gson();
    private Class<KeyReturn> clazz;
    private final Response.Listener<KeyReturn> listener;
    private String params;
    private Map<String,String> map;

    public KeyReturnRequest(String url,RegistrationObject RO,
                            Response.Listener<KeyReturn> listener,Response.ErrorListener errorListener){
        super(Method.POST,url,errorListener);
        this.listener = listener;

        Gson gson = new Gson();
        String json = gson.toJson(RO);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            this.params = jsonObject.toString();
        }
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        map = gson.fromJson(json, type);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return params == null ? super.getBody() : params.getBytes();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }

    @Override
    protected void deliverResponse(KeyReturn keyReturn) {

        listener.onResponse(keyReturn);
    }

    @Override
    protected Response<KeyReturn> parseNetworkResponse(NetworkResponse networkResponse) {
        try {
            String keyReturnString = new String(
                    networkResponse.data,
                    HttpHeaderParser.parseCharset(networkResponse.headers));
            return Response.success(
                    gson.fromJson(keyReturnString, clazz),
                    HttpHeaderParser.parseCacheHeaders(networkResponse));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}
