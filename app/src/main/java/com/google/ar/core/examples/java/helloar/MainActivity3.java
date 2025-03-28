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
import com.google.ar.core.examples.java.common.entityModel.Level;
import com.google.ar.core.examples.java.common.entityModel.Point;
import com.google.ar.core.examples.java.common.entityModel.Storage;
import com.google.ar.core.examples.java.common.httpConnection.HttpConnectionHandler;

import java.io.IOException;

import okhttp3.Response;

public class MainActivity3 extends AppCompatActivity {
    ArrayAdapter<Point> adapter;
    ListView pointList;
    Button backButton;
    SwipeRefreshLayout swipeRefreshLayout;

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
        String venueIndex = getIntent().getStringExtra("venueIndex");
        ((TextView) findViewById(R.id.textView3)).setText(Storage.INSTANCE.getVenues().get(Integer.parseInt(venueIndex)).getName());
        swipeRefreshLayout.setOnRefreshListener(() -> populateList(url, venueIndex, swipeRefreshLayout));


        populateList(url, venueIndex, swipeRefreshLayout);
    }

    private void populateList(String url, String venueIndex, SwipeRefreshLayout swipeRefreshLayout) {
        try {
            Response r = HttpConnectionHandler.INSTANCE.newRequest("http://" + url + "/data/venuedata/" + Storage.INSTANCE.getVenues().get(Integer.parseInt(venueIndex)).getId());
            if (!r.isSuccessful()) {
                throw new IOException();
            }
            Storage.INSTANCE.setLevels(HttpConnectionHandler.INSTANCE.getResponseFromJson(r, Level.LIST_TYPE_TOKEN));
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                    Storage.INSTANCE.getLevels().stream()
                            .flatMap(x -> x.getPointSet().stream()).toList());
            pointList.setAdapter(adapter);
            pointList.setOnItemClickListener((adapter, v, position, id) -> itemSelected((AdapterView<ArrayAdapter<Point>>) adapter, position));
            Toast.makeText(MainActivity3.this, "Points loaded", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(MainActivity3.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        swipeRefreshLayout.setRefreshing(false);

    }

    private void itemSelected(AdapterView<ArrayAdapter<Point>> adapter, int position) {
        Toast.makeText(MainActivity3.this, "You clicked on " + ((Point) adapter.getItemAtPosition(position)).getName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity3.this, MainActivity4.class);
        startActivity(intent);
    }
}
