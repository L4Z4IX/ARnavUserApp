package com.google.ar.core.examples.java.helloar;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.ar.core.examples.java.common.entityModel.Storage;
import com.google.ar.core.examples.java.common.entityModel.Venue;
import com.google.ar.core.examples.java.common.httpConnection.HttpConnectionHandler;
import com.google.ar.core.examples.java.common.listHelpers.CustomAdapter;
import com.google.ar.core.examples.java.common.listHelpers.FormHandler;

import java.io.IOException;

import okhttp3.Response;

public class AdminActivity2 extends AppCompatActivity {
    String url;
    RecyclerView venueList;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin2);

        TextView serverName = findViewById(R.id.admin_server_name);
        venueList = findViewById(R.id.admin_venue_list);
        swipeRefreshLayout = findViewById(R.id.admin_venue_refreshLayout);
        ImageButton addButton = findViewById(R.id.venue_add_button);

        serverName.setText(getIntent().getStringExtra("name"));
        url = getIntent().getStringExtra("url");
        venueList.setLayoutManager(new LinearLayoutManager(this));
        refreshData();
        addButton.setOnClickListener(v -> {
            Toast.makeText(AdminActivity2.this, "Clicked on add", Toast.LENGTH_SHORT).show();
        });
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);


    }

    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);
        Storage.INSTANCE.clearInstance();
        try {
            Response r = HttpConnectionHandler.INSTANCE.newRequest(url + "/data/venues");
            if (!r.isSuccessful()) {
                throw new IOException();
            }
            Storage.INSTANCE.setVenues(HttpConnectionHandler.INSTANCE.getResponseFromJson(r, Venue.LIST_TYPE_TOKEN));
            CustomAdapter<Venue> adapter = new CustomAdapter<>(Storage.INSTANCE.getVenues(), new FormHandler<Venue>() {
                @Override
                public void onEditButtonClick(Venue item) {
                    Toast.makeText(AdminActivity2.this, "Edit of " + item.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRemoveButtonClick(Venue item) {
                    Toast.makeText(AdminActivity2.this, "Remove of " + item.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            venueList.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            Toast.makeText(AdminActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
