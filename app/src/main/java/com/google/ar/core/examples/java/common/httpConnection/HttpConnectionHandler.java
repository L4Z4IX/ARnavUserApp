package com.google.ar.core.examples.java.common.httpConnection;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;

public class HttpConnectionHandler {
    private final OkHttpClient client=new OkHttpClient();

    public Response newRequest(String url) throws IOException {
        return client.newCall(new Request.Builder().url(url).build()).execute();
    }
    public String getResponseString(Response response) throws IOException {
        return response.isSuccessful()?response.body().string():null;
    }
    public <T> T getResponseFromJson(Response response, Class<T> clazz) throws IOException {
        return new Gson().fromJson(response.body().string(), clazz);
    }
}
