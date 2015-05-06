package com.trippntechnology.doorsecurity;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;


public class KeyReturnRequest extends Request<KeyReturn> {

    private final Gson gson = new Gson();
    private Class<KeyReturn> clazz;
    private final Response.Listener<KeyReturn> listener;
    private final RegistrationObject registrationObject;
    ProgressDialog progressDialog;


    public KeyReturnRequest(String url,RegistrationObject ro,ProgressDialog progressDialog,
                            Response.Listener<KeyReturn> listener,Response.ErrorListener errorListener){
        super(Method.POST,url,errorListener);
        this.progressDialog = progressDialog;
        progressDialog.show();
        progressDialog.setTitle("Registering");
        progressDialog.setMessage("Getting key from server");
        this.registrationObject = ro;
        this.listener = listener;

    }


    @Override
    protected void deliverResponse(KeyReturn keyReturn) {
        progressDialog.dismiss();

        listener.onResponse(keyReturn);
    }

    @Override
    protected Response<KeyReturn> parseNetworkResponse(NetworkResponse networkResponse) {
        try {
            String keyReturnString = new String(
                    networkResponse.data,
                    HttpHeaderParser.parseCharset(networkResponse.headers));
            progressDialog.dismiss();
            return Response.success(
                    gson.fromJson(keyReturnString, clazz),
                    HttpHeaderParser.parseCacheHeaders(networkResponse));
        } catch (UnsupportedEncodingException e) {
            progressDialog.dismiss();
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            progressDialog.dismiss();
            return Response.error(new ParseError(e));
        }
    }
}
