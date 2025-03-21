package com.google.ar.core.examples.java.common.httpConnection;

import android.os.StrictMode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpConnectionHandler {
    private final OkHttpClient client = new OkHttpClient();
    public static final HttpConnectionHandler INSTANCE = new HttpConnectionHandler();

    private HttpConnectionHandler() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
    }

    public Response newRequest(String url) throws IOException {
        return client.newCall(new Request.Builder().url(url).build()).execute();
    }

    public String getResponseString(Response response) throws IOException {
        return response.isSuccessful() ? response.body().string() : null;
    }

    public <T> T getResponseFromJson(Response response, TypeToken<T> type) throws IOException {
        return new Gson().fromJson(response.body().string(), type);
    }
}
