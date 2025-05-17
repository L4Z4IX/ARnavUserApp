package com.google.ar.core.examples.java.helloar;

import android.content.Intent;
import android.os.Bundle;
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
        String url = getIntent().getStringExtra("url");
        venueId = getIntent().getStringExtra("venueId");
        ((TextView) findViewById(R.id.textView3)).setText(Storage.INSTANCE.getVenues().get(Integer.parseInt(venueId)).getName());
        swipeRefreshLayout.setOnRefreshListener(() -> populateList(url, venueId, swipeRefreshLayout));


        populateList(url, venueId, swipeRefreshLayout);
    }

    private void populateList(String url, String venueIndex, SwipeRefreshLayout swipeRefreshLayout) {
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

            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                    Storage.INSTANCE.getLevels().stream()
                            .flatMap(x -> x.getPoints().stream()).toList());
            pointList.setAdapter(adapter);
            pointList.setOnItemClickListener((adapter, v, position, id) -> itemSelected((AdapterView<ArrayAdapter<Point>>) adapter, position));
            Toast.makeText(MainActivity3.this, "Points loaded", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(MainActivity3.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            System.err.println("Error on points load:");
            e.printStackTrace();
        }
        swipeRefreshLayout.setRefreshing(false);

    }

    private void itemSelected(AdapterView<ArrayAdapter<Point>> adapter, int position) {
        Toast.makeText(MainActivity3.this, "You clicked on " + ((Point) adapter.getItemAtPosition(position)).getName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity3.this, MainActivity4.class);
        intent.putExtra("pointId", ((Point) adapter.getItemAtPosition(position)).getId() + "");
        intent.putExtra("venueId", venueId);
        startActivity(intent);
    }
}
