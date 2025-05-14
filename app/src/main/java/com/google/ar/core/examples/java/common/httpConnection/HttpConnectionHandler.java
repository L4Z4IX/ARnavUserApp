package com.google.ar.core.examples.java.common.httpConnection;

import android.os.StrictMode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpConnectionHandler {

    CookieJar cookieJar = new CookieJar() {
        private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url, cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            return Objects.requireNonNull(cookieStore.getOrDefault(url, new ArrayList<>()));
        }
    };
    private final OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build();
    public static final HttpConnectionHandler INSTANCE = new HttpConnectionHandler();

    private HttpConnectionHandler() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
    }

    public Response newRequest(String url) throws IOException {
        return client.newCall(new Request.Builder().url(url).build()).execute();
    }

    public Response doPost(String url, RequestBody requestBody) throws IOException {
        return client.newCall(new Request.Builder().url(url).post(requestBody).build()).execute();
    }

    public String getResponseString(Response response) throws IOException {
        return response.isSuccessful() ? response.body().string() : null;
    }

    public <T> T getResponseFromJson(Response response, TypeToken<T> type) throws IOException {
        return new Gson().fromJson(response.body().string(), type);
    }
}
