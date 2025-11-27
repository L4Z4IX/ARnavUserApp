package hu.pte.mik.l4z4ix.src.Components.httpConnection;

import android.os.StrictMode;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpConnectionHandler {
    private static final HttpConnectionHandler INSTANCE = new HttpConnectionHandler();
    private final ObjectMapper mapper = new ObjectMapper().setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    CookieJar cookieJar = new CookieJar() {
        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();


        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            return Objects.requireNonNull(cookieStore.getOrDefault(url.host(), new ArrayList<>()));
        }
    };
    private final OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build();


    public static HttpConnectionHandler getInstance() {
        return INSTANCE;
    }

    private HttpConnectionHandler() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
    }

    public Response doRequest(String url) throws IOException {
        return client.newCall(new Request.Builder().url(url).build()).execute();
    }

    public Response doRequest(String url, Object DTO, RequestType requestType) throws IOException {
        Headers headers = new Headers.Builder().add("Content-Type: application/json").build();
        String json = mapper.writeValueAsString(DTO);
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(json, JSON);
        return switch (requestType) {
            case GET -> client.newCall(new Request.Builder().url(url).build()).execute();
            case POST ->
                    client.newCall(new Request.Builder().url(url).headers(headers).post(requestBody).build()).execute();
            case PATCH ->
                    client.newCall(new Request.Builder().url(url).headers(headers).patch(requestBody).build()).execute();
            case DELETE ->
                    client.newCall(new Request.Builder().url(url).headers(headers).delete(requestBody).build()).execute();
        };

    }

    public String getResponseString(Response response) throws IOException {
        return response.isSuccessful() ? response.body().string() : null;
    }

    public <T> T getResponseFromJson(Response response, TypeToken<T> type) throws IOException {
        return new Gson().fromJson(response.body().string(), type);
    }
}
