package com.google.ar.core.examples.java.helloar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.google.ar.core.examples.java.common.entityModel.Connection;
import com.google.ar.core.examples.java.common.entityModel.Level;
import com.google.ar.core.examples.java.common.entityModel.Point;
import com.google.ar.core.examples.java.common.entityModel.Storage;
import com.google.ar.core.examples.java.common.httpConnection.HttpConnectionHandler;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Response;

public class MainActivity3 extends AppCompatActivity {
    private ArrayAdapter<Point> adapter;
    private ListView pointList;
    private Button backButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String venueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        TextInputLayout searchBar = findViewById(R.id.textInputLayout);
        pointList = findViewById(R.id.pointList);
        backButton = findViewById(R.id.backButton2);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


        backButton.setOnClickListener(v -> {
            finish();
        });
        searchBar.getEditText()
                .addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                            }

                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                populateList(s);
                                            }

                                            @Override
                                            public void afterTextChanged(Editable s) {

                                            }
                                        }
                );
        String url = getIntent().getStringExtra("url");
        venueId = getIntent().getStringExtra("venueId");
        ((TextView) findViewById(R.id.textView3)).setText(Storage.INSTANCE.getVenues().get(Integer.parseInt(venueId)).getName());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            searchBar.getEditText().setText("");
            getData(url, venueId);
        });

        getData(url, venueId);
    }

    private void populateList(CharSequence filter) {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                Storage.INSTANCE.getLevels().stream()
                        .flatMap(x -> x.getPoints().stream().filter(y -> y.getName().contains(filter))).toList());
        pointList.setAdapter(adapter);
        pointList.setOnItemClickListener((adapter, v, position, id) -> itemSelected((AdapterView<ArrayAdapter<Point>>) adapter, position));
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getData(String url, String venueIndex) {
        try {
            Response r = HttpConnectionHandler.INSTANCE.newRequest("http://" + url + "/data/venuedata/" + Storage.INSTANCE.getVenues().get(Integer.parseInt(venueIndex)).getId());
            if (!r.isSuccessful()) {
                throw new IOException();
            }
            Storage.INSTANCE.setLevels(HttpConnectionHandler.INSTANCE.getResponseFromJson(r, Level.LIST_TYPE_TOKEN));
            Response r2 = HttpConnectionHandler.INSTANCE.newRequest("http://" + url + "/data/connectionsByVenue?venueId=" + Storage.INSTANCE.getVenues().get(Integer.parseInt(venueIndex)).getId());
            if (!r2.isSuccessful()) {
                throw new IOException();
            }
            Storage.INSTANCE.setConnections(HttpConnectionHandler.INSTANCE.getResponseFromJson(r2, Connection.LIST_TYPE_TOKEN));
            Toast.makeText(MainActivity3.this, "Points loaded", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(MainActivity3.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            System.err.println("Error on points load:");
            e.printStackTrace();
        }
        populateList("");
    }

    private void itemSelected(AdapterView<ArrayAdapter<Point>> adapter, int position) {
        Intent intent = new Intent(MainActivity3.this, MainActivity4.class);
        intent.putExtra("point", (new Gson()).toJson(adapter.getItemAtPosition(position), Point.class));
        intent.putExtra("venueId", venueId);
        intent.putExtra("type", "user");
        startActivity(intent);
    }
}
