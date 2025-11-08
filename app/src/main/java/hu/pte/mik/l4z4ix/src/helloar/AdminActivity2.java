package hu.pte.mik.l4z4ix.src.helloar;

import android.content.Intent;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;

import hu.pte.mik.l4z4ix.src.common.dto.VenueDTOs;
import hu.pte.mik.l4z4ix.src.common.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.common.entityModel.Venue;
import hu.pte.mik.l4z4ix.src.common.httpConnection.HttpConnectionHandler;
import hu.pte.mik.l4z4ix.src.common.listHelpers.CustomAdapter;
import hu.pte.mik.l4z4ix.src.common.listHelpers.CustomViewHolder;
import hu.pte.mik.l4z4ix.src.common.listHelpers.FormHandler;
import okhttp3.Response;


public class AdminActivity2 extends AppCompatActivity {
    String url;
    RecyclerView venueList;
    SwipeRefreshLayout swipeRefreshLayout;
    ObjectMapper mapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mapper = new ObjectMapper();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin2);

        TextView serverName = findViewById(R.id.admin_venue_name);
        venueList = findViewById(R.id.admin_venue_list);
        swipeRefreshLayout = findViewById(R.id.admin_venue_refreshLayout);
        ImageButton addButton = findViewById(R.id.level_add_button);
        Button backButton = findViewById(R.id.backButtonAdmin2);

        backButton.setOnClickListener(v -> {
            finish();
        });

        serverName.setText(getIntent().getStringExtra("name").trim());
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
                            VenueDTOs.addVenueDTO addVenueDTO = new VenueDTOs.addVenueDTO(name);
                            Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/addVenue", addVenueDTO);
                            if (resp.isSuccessful()) {
                                Toast.makeText(AdminActivity2.this, resp.body().string(), Toast.LENGTH_SHORT).show();
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

            CustomAdapter<Venue, CustomViewHolder> adapter = new CustomAdapter<>(R.layout.admin_list_item, Storage.INSTANCE.getVenues(), new FormHandler<>() {
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
                                    VenueDTOs.setVenueNameDTO setVenueNameDTO = new VenueDTOs.setVenueNameDTO(item.getId(), name);
                                    Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/setVenueName", setVenueNameDTO);
                                    if (resp.isSuccessful()) {
                                        Toast.makeText(AdminActivity2.this, resp.body().string(), Toast.LENGTH_SHORT).show();
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
                    AlertDialog dialog = new AlertDialog.Builder(AdminActivity2.this).setTitle("Confirmation").setMessage("Confirm the removal of " + item.getName() + " venue. Warning: all corespondent levels, points and connections will be lost!").setPositiveButton(
                                    "Confirm", (dialogInterface, i) -> {
                                        try {
                                            VenueDTOs.delVenueDTO delVenueDTO = new VenueDTOs.delVenueDTO(item.getId());
                                            Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/delVenue", delVenueDTO);
                                            if (resp.isSuccessful()) {
                                                Toast.makeText(AdminActivity2.this, resp.body().string(), Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(AdminActivity2.this, AdminActivity3.class);
                intent.putExtra("venue", new Gson().toJson(item));
                intent.putExtra("url", url);
                startActivity(intent);
            }, CustomViewHolder.class);
            venueList.setAdapter(adapter);

            swipeRefreshLayout.setRefreshing(false);

        } catch (IOException e) {
            Toast.makeText(AdminActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
