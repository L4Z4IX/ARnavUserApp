package com.google.ar.core.examples.java.helloar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.ar.core.examples.java.common.dto.ConnectionDTOs;
import com.google.ar.core.examples.java.common.entityModel.Connection;
import com.google.ar.core.examples.java.common.entityModel.Level;
import com.google.ar.core.examples.java.common.entityModel.Point;
import com.google.ar.core.examples.java.common.entityModel.Storage;
import com.google.ar.core.examples.java.common.entityModel.Venue;
import com.google.ar.core.examples.java.common.httpConnection.HttpConnectionHandler;
import com.google.ar.core.examples.java.common.listHelpers.CustomConnectionAdapter;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Response;

public class AdminActivity5 extends AppCompatActivity {
    String url;
    Point point;
    Level level;
    Venue venue;
    RecyclerView connectionList;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin5);
        swipeRefreshLayout = findViewById(R.id.admin_connection_refreshLayout);
        connectionList = findViewById(R.id.admin_connection_list);
        TextView pointName = findViewById(R.id.admin_pointConnection_name);
        Button backButton = findViewById(R.id.backButtonAdmin5);

        backButton.setOnClickListener(v -> {
            finish();
        });

        url = getIntent().getStringExtra("url");
        venue = new Gson().fromJson(getIntent().getStringExtra("venue"), Venue.class);
        level = Storage.INSTANCE.getLevels().stream().filter(x -> x.getId() == Integer.parseInt(getIntent().getStringExtra("levelId"))).findFirst().get();
        point = new Gson().fromJson(getIntent().getStringExtra("point"), Point.class);

        pointName.setText(point.getName());

        connectionList.setLayoutManager(new LinearLayoutManager(this));
        refreshData();


        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);
        try {
            Response r = HttpConnectionHandler.INSTANCE.newRequest(url + "/data/connectionsByVenue?venueId=" + venue.getId());
            if (!r.isSuccessful()) {
                throw new IOException();
            }
            CustomConnectionAdapter connectionAdapter = new CustomConnectionAdapter(Storage.INSTANCE.getLevels().stream().flatMap(x -> x.getPoints().stream()).toList(), HttpConnectionHandler.INSTANCE.getResponseFromJson(r, Connection.LIST_TYPE_TOKEN), (item, state) -> {
                Response res;
                try {
                    if (state) {
                        ConnectionDTOs.addConnectionDTO addConnectionDTO = new ConnectionDTOs.addConnectionDTO(point.getId(), item.getId());
                        res = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/addConnection", addConnectionDTO);
                    } else {
                        ConnectionDTOs.delConnectionDTO delConnectionDTO = new ConnectionDTOs.delConnectionDTO(point.getId(), item.getId());
                        res = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/delConnection", delConnectionDTO);
                    }
                    if (res.isSuccessful()) {
                        Toast.makeText(AdminActivity5.this, res.body().string(), Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(AdminActivity5.this, "Something went wrong: " + res.code(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(AdminActivity5.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }, point);
            connectionList.setAdapter(connectionAdapter);


        } catch (IOException e) {
            Toast.makeText(AdminActivity5.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        } finally {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
