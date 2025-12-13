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

import hu.pte.mik.l4z4ix.src.Components.dto.LevelDTOs;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Level;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Venue;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.DataManager;
import hu.pte.mik.l4z4ix.src.Components.listHelpers.CustomAdapter;
import hu.pte.mik.l4z4ix.src.Components.listHelpers.CustomViewHolder;
import hu.pte.mik.l4z4ix.src.Components.listHelpers.FormHandler;

public class AdminLevelActivity extends AppCompatActivity {
    private Venue venue;
    private RecyclerView levelList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final DataManager dataManager = DataManager.getManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_level);

        levelList = findViewById(R.id.admin_level_list);
        swipeRefreshLayout = findViewById(R.id.admin_level_refreshLayout);
        ImageButton addButton = findViewById(R.id.level_add_button);
        TextView venueName = findViewById(R.id.admin_venueLevel_name);
        Button backButton = findViewById(R.id.backButtonAdmin3);

        backButton.setOnClickListener(v -> {
            finish();
        });
        venue = new Gson().fromJson(getIntent().getStringExtra("venue"), Venue.class);
        venueName.setText(venue.getName());
        levelList.setLayoutManager(new LinearLayoutManager(this));
        refreshData();
        addButton.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.admin_level_dialog, null);

            EditText inputName = view.findViewById(R.id.pointName);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Add level")
                    .setView(view)
                    .setPositiveButton("Add", (dialogInterface, i) -> {
                        String name = inputName.getText().toString();
                        try {
                            LevelDTOs.addLevelDTO addLevelDTO = new LevelDTOs.addLevelDTO(name, venue.getId());
                            Toast.makeText(AdminLevelActivity.this, dataManager.addLevel(addLevelDTO), Toast.LENGTH_SHORT).show();
                            refreshData();
                        } catch (IOException e) {
                            Toast.makeText(AdminLevelActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            dataManager.requestVenueData(venue.getId());

            CustomAdapter<Level, CustomViewHolder> adapter = new CustomAdapter<>(R.layout.admin_list_item, Storage.INSTANCE.getLevels(), new FormHandler<>() {
                @Override
                public void onEditButtonClick(Level item) {
                    LayoutInflater inflater = LayoutInflater.from(AdminLevelActivity.this);
                    View view = inflater.inflate(R.layout.admin_level_dialog, null);

                    EditText inputName = view.findViewById(R.id.pointName);
                    inputName.setText(item.getName());

                    AlertDialog dialog = new AlertDialog.Builder(AdminLevelActivity.this)
                            .setTitle("Edit Level")
                            .setView(view)
                            .setPositiveButton("Edit", (dialogInterface, i) -> {
                                String name = inputName.getText().toString();
                                try {
                                    LevelDTOs.setLevelNameDTO setLevelNameDTO = new LevelDTOs.setLevelNameDTO(name, item.getId());
                                    Toast.makeText(AdminLevelActivity.this, dataManager.updateLevel(setLevelNameDTO), Toast.LENGTH_SHORT).show();
                                    refreshData();
                                } catch (IOException e) {
                                    Toast.makeText(AdminLevelActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();

                    dialog.show();
                }

                @Override
                public void onRemoveButtonClick(Level item) {
                    AlertDialog dialog = new AlertDialog.Builder(AdminLevelActivity.this).setTitle("Confirmation").setMessage("Confirm the removal of " + item.getName() + " level. Warning: All corespondent points and connections will be lost!").setPositiveButton(
                                    "Confirm", (dialogInterface, i) -> {
                                        try {
                                            LevelDTOs.delLevelDTO delLevelDTO = new LevelDTOs.delLevelDTO(item.getId());
                                            Toast.makeText(AdminLevelActivity.this, dataManager.deleteLevel(delLevelDTO), Toast.LENGTH_SHORT).show();
                                            refreshData();
                                        } catch (IOException e) {
                                            Toast.makeText(AdminLevelActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();
                    dialog.show();
                }
            }, item -> {
                Intent intent = new Intent(AdminLevelActivity.this, AdminPointActivity.class);
                intent.putExtra("levelId", item.getId() + "");
                intent.putExtra("venue", new Gson().toJson(venue));
                startActivity(intent);
            }, CustomViewHolder.class);
            levelList.setAdapter(adapter);

            swipeRefreshLayout.setRefreshing(false);

        } catch (IOException e) {
            Toast.makeText(AdminLevelActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
