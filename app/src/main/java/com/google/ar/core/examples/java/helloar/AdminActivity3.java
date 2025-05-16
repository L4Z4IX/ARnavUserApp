package com.google.ar.core.examples.java.helloar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.ar.core.examples.java.common.entityModel.Level;
import com.google.ar.core.examples.java.common.entityModel.Storage;
import com.google.ar.core.examples.java.common.entityModel.Venue;
import com.google.ar.core.examples.java.common.httpConnection.HttpConnectionHandler;
import com.google.ar.core.examples.java.common.listHelpers.CustomAdapter;
import com.google.ar.core.examples.java.common.listHelpers.FormHandler;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Response;

public class AdminActivity3 extends AppCompatActivity {
    String url;
    Venue venue;
    RecyclerView levelList;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin3);

        levelList = findViewById(R.id.admin_level_list);
        swipeRefreshLayout = findViewById(R.id.admin_level_refreshLayout);
        ImageButton addButton = findViewById(R.id.level_add_button);
        TextView venueName = findViewById(R.id.admin_venueLevel_name);
        Button backButton = findViewById(R.id.backButtonAdmin3);

        backButton.setOnClickListener(v -> {
            finish();
        });

        url = getIntent().getStringExtra("url");
        venue = new Gson().fromJson(getIntent().getStringExtra("venue"), Venue.class);
        venueName.setText(venue.getName());
        levelList.setLayoutManager(new LinearLayoutManager(this));
        refreshData();
        addButton.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.admin_level_dialog, null);

            EditText inputName = view.findViewById(R.id.levelName);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Add level")
                    .setView(view)
                    .setPositiveButton("Add", (dialogInterface, i) -> {
                        String name = inputName.getText().toString();
                        try {
                            Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/addLevel?levelName=" + name + "&venueId=" + venue.getId());
                            if (resp.isSuccessful()) {
                                Toast.makeText(AdminActivity3.this, "Added level", Toast.LENGTH_SHORT).show();
                                refreshData();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(AdminActivity3.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
            Response r = HttpConnectionHandler.INSTANCE.newRequest(url + "/data/venuedata/" + venue.getId());
            if (!r.isSuccessful()) {
                throw new IOException();
            }
            Storage.INSTANCE.setLevels(HttpConnectionHandler.INSTANCE.getResponseFromJson(r, Level.LIST_TYPE_TOKEN));

            CustomAdapter<Level> adapter = new CustomAdapter<>(Storage.INSTANCE.getLevels(), new FormHandler<Level>() {
                @Override
                public void onEditButtonClick(Level item) {
                    LayoutInflater inflater = LayoutInflater.from(AdminActivity3.this);
                    View view = inflater.inflate(R.layout.admin_level_dialog, null);

                    EditText inputName = view.findViewById(R.id.levelName);
                    inputName.setText(item.getName());

                    AlertDialog dialog = new AlertDialog.Builder(AdminActivity3.this)
                            .setTitle("Edit Level")
                            .setView(view)
                            .setPositiveButton("Edit", (dialogInterface, i) -> {
                                String name = inputName.getText().toString();
                                try {
                                    Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/setLevelName?id=" + item.getId() + "&levelName=" + name);
                                    if (resp.isSuccessful()) {
                                        Toast.makeText(AdminActivity3.this, "Updated level", Toast.LENGTH_SHORT).show();
                                        refreshData();
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(AdminActivity3.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();

                    dialog.show();
                }

                @Override
                public void onRemoveButtonClick(Level item) {
                    AlertDialog dialog = new AlertDialog.Builder(AdminActivity3.this).setTitle("Confirmation").setMessage("Confirm the removal of " + item.getName() + " level").setPositiveButton(
                                    "Confirm", (dialogInterface, i) -> {
                                        try {
                                            Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/delLevel?levelId=" + item.getId());
                                            if (resp.isSuccessful()) {
                                                Toast.makeText(AdminActivity3.this, "Removed " + item.getName(), Toast.LENGTH_SHORT).show();
                                                refreshData();
                                            }
                                        } catch (IOException e) {
                                            Toast.makeText(AdminActivity3.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();
                    dialog.show();
                }
            }, item -> {
                Toast.makeText(AdminActivity3.this, "Selected " + item.getName(), Toast.LENGTH_SHORT).show();
            });
            levelList.setAdapter(adapter);

            swipeRefreshLayout.setRefreshing(false);

        } catch (IOException e) {
            Toast.makeText(AdminActivity3.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
