package com.google.ar.core.examples.java.helloar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.admin_venue_dialog, null);

            EditText inputName = view.findViewById(R.id.input_name);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Add venue")
                    .setView(view)
                    .setPositiveButton("Add", (dialogInterface, i) -> {
                        String name = inputName.getText().toString();
                        try {
                            Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/addVenue?venueName=" + name);
                            if (resp.isSuccessful()) {
                                Toast.makeText(AdminActivity2.this, "Added venue", Toast.LENGTH_SHORT).show();
                                refreshData();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(AdminActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                    .create();

            dialog.show();
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
                    LayoutInflater inflater = LayoutInflater.from(AdminActivity2.this);
                    View view = inflater.inflate(R.layout.admin_venue_dialog, null);

                    EditText inputName = view.findViewById(R.id.input_name);
                    inputName.setText(item.getName());

                    AlertDialog dialog = new AlertDialog.Builder(AdminActivity2.this)
                            .setTitle("Edit venue")
                            .setView(view)
                            .setPositiveButton("Edit", (dialogInterface, i) -> {
                                String name = inputName.getText().toString();
                                try {
                                    Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/setVenueName?id=" + item.getId() + "&name=" + name);
                                    if (resp.isSuccessful()) {
                                        Toast.makeText(AdminActivity2.this, "Updated venue", Toast.LENGTH_SHORT).show();
                                        refreshData();
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(AdminActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();

                    dialog.show();
                }

                @Override
                public void onRemoveButtonClick(Venue item) {
                    AlertDialog dialog = new AlertDialog.Builder(AdminActivity2.this).setTitle("Confirmation").setMessage("Confirm the removal of " + item.getName() + " venue").setPositiveButton(
                                    "Confirm", (dialogInterface, i) -> {
                                        try {
                                            Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/delVenue?id=" + item.getId());
                                            if (resp.isSuccessful()) {
                                                Toast.makeText(AdminActivity2.this, "Removed " + item.getName(), Toast.LENGTH_SHORT).show();
                                                refreshData();
                                            }
                                        } catch (IOException e) {
                                            Toast.makeText(AdminActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();
                    dialog.show();
                }
            }, item -> {
                Toast.makeText(AdminActivity2.this, "Selected " + item.getName(), Toast.LENGTH_SHORT).show();
            });
            venueList.setAdapter(adapter);

            swipeRefreshLayout.setRefreshing(false);

        } catch (IOException e) {
            Toast.makeText(AdminActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
