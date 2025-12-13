package hu.pte.mik.l4z4ix.src.ArApplication;

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

import com.google.gson.Gson;

import java.io.IOException;

import hu.pte.mik.l4z4ix.src.Components.dto.VenueDTOs;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Venue;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.DataManager;
import hu.pte.mik.l4z4ix.src.Components.listHelpers.CustomAdapter;
import hu.pte.mik.l4z4ix.src.Components.listHelpers.CustomViewHolder;
import hu.pte.mik.l4z4ix.src.Components.listHelpers.FormHandler;


public class AdminVenueActivity extends AppCompatActivity {
    private RecyclerView venueList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final DataManager dataManager = DataManager.getManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_venue);

        TextView serverName = findViewById(R.id.admin_venue_name);
        venueList = findViewById(R.id.admin_venue_list);
        swipeRefreshLayout = findViewById(R.id.admin_venue_refreshLayout);
        ImageButton addButton = findViewById(R.id.level_add_button);
        Button backButton = findViewById(R.id.backButtonAdmin2);

        backButton.setOnClickListener(v -> {
            finish();
        });

        serverName.setText(getIntent().getStringExtra("name").trim());
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
                            Toast.makeText(AdminVenueActivity.this, dataManager.addVenue(addVenueDTO), Toast.LENGTH_SHORT).show();
                            refreshData();
                        } catch (IOException e) {
                            Toast.makeText(AdminVenueActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        try {
            dataManager.requestVenues();

            CustomAdapter<Venue, CustomViewHolder> adapter = new CustomAdapter<>(R.layout.admin_list_item, Storage.INSTANCE.getVenues(), new FormHandler<>() {
                @Override
                public void onEditButtonClick(Venue item) {
                    LayoutInflater inflater = LayoutInflater.from(AdminVenueActivity.this);
                    View view = inflater.inflate(R.layout.admin_venue_dialog, null);

                    EditText inputName = view.findViewById(R.id.input_name);
                    inputName.setText(item.getName());

                    AlertDialog dialog = new AlertDialog.Builder(AdminVenueActivity.this)
                            .setTitle("Edit venue")
                            .setView(view)
                            .setPositiveButton("Edit", (dialogInterface, i) -> {
                                String name = inputName.getText().toString();
                                try {
                                    VenueDTOs.setVenueNameDTO setVenueNameDTO = new VenueDTOs.setVenueNameDTO(item.getId(), name);
                                    Toast.makeText(AdminVenueActivity.this, dataManager.updateVenue(setVenueNameDTO), Toast.LENGTH_SHORT).show();
                                    refreshData();
                                } catch (IOException e) {
                                    Toast.makeText(AdminVenueActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();

                    dialog.show();
                }

                @Override
                public void onRemoveButtonClick(Venue item) {
                    AlertDialog dialog = new AlertDialog.Builder(AdminVenueActivity.this).setTitle("Confirmation").setMessage("Confirm the removal of " + item.getName() + " venue. Warning: all corespondent levels, points and connections will be lost!").setPositiveButton(
                                    "Confirm", (dialogInterface, i) -> {
                                        try {
                                            VenueDTOs.delVenueDTO delVenueDTO = new VenueDTOs.delVenueDTO(item.getId());
                                            Toast.makeText(AdminVenueActivity.this, dataManager.deleteVenue(delVenueDTO), Toast.LENGTH_SHORT).show();
                                            refreshData();

                                        } catch (IOException e) {
                                            Toast.makeText(AdminVenueActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();
                    dialog.show();
                }
            }, item -> {
                Intent intent = new Intent(AdminVenueActivity.this, AdminLevelActivity.class);
                intent.putExtra("venue", new Gson().toJson(item));
                startActivity(intent);
            }, CustomViewHolder.class);
            venueList.setAdapter(adapter);

            swipeRefreshLayout.setRefreshing(false);

        } catch (IOException e) {
            Toast.makeText(AdminVenueActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
