package hu.pte.mik.l4z4ix.src.helloar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import hu.pte.mik.l4z4ix.src.common.dto.PointDTOs;
import hu.pte.mik.l4z4ix.src.common.entityModel.Level;
import hu.pte.mik.l4z4ix.src.common.entityModel.Point;
import hu.pte.mik.l4z4ix.src.common.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.common.entityModel.Venue;
import hu.pte.mik.l4z4ix.src.common.httpConnection.HttpConnectionHandler;
import hu.pte.mik.l4z4ix.src.common.listHelpers.CustomPointAdapter;
import hu.pte.mik.l4z4ix.src.common.listHelpers.PointFormHandler;
import hu.pte.mik.l4z4ix.src.common.navigation.LocationProvider;
import okhttp3.Response;

public class AdminActivity4 extends AppCompatActivity {
    String url;
    Level level;
    RecyclerView pointList;
    SwipeRefreshLayout swipeRefreshLayout;
    Venue venue;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin4);

        pointList = findViewById(R.id.admin_point_list);
        swipeRefreshLayout = findViewById(R.id.admin_point_refreshLayout);
        ImageButton addButton = findViewById(R.id.point_add_button);
        TextView levelName = findViewById(R.id.admin_levelPoint_name);
        Button backButton = findViewById(R.id.backButtonAdmin4);

        backButton.setOnClickListener(v -> {
            finish();
        });

        url = getIntent().getStringExtra("url");
        venue = new Gson().fromJson(getIntent().getStringExtra("venue"), Venue.class);
        level = Storage.INSTANCE.getLevels().stream().filter(x -> x.getId() == Integer.parseInt(getIntent().getStringExtra("levelId"))).findFirst().get();
        levelName.setText(level.getName());
        pointList.setLayoutManager(new LinearLayoutManager(this));
        refreshData();
        addButton.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.admin_point_dialog, null);

            EditText inputName = view.findViewById(R.id.pointName);
            Spinner levelSpinner = view.findViewById(R.id.levelSpinner);
            EditText inputAccuracy = view.findViewById(R.id.accText);
            EditText inputlatitude = view.findViewById(R.id.latText);
            EditText inputlongitude = view.findViewById(R.id.lngText);
            EditText inputAltitude = view.findViewById(R.id.altText);
            Button measureButton = view.findViewById(R.id.measureButton);

            AtomicBoolean isMeasuring = new AtomicBoolean(false);
            AtomicReference<Timer> locationTimer = new AtomicReference<>(new Timer());

            measureButton.setOnClickListener(vv -> {
                if (isMeasuring.get()) {
                    locationTimer.get().cancel();
                    locationTimer.set(new Timer());
                    measureButton.setText("MEASURE");
                    LocationProvider.getInstance(null).stop();
                    isMeasuring.set(false);

                } else {
                    LocationProvider.getInstance(null).start();
                    locationTimer.get().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Location l = LocationProvider.getInstance(null).getCurrentLocation();
                            if (l != null) {
                                runOnUiThread(() -> {
                                    inputAccuracy.setText(l.getAccuracy() + "");
                                    inputlatitude.setText(l.getLatitude() + "");
                                    inputlongitude.setText(l.getLongitude() + "");
                                    inputAltitude.setText(l.getAltitude() + "");

                                    measureButton.setText("STOP");
                                });
                            }
                        }
                    }, 2000, 1000);
                    isMeasuring.set(true);
                }
            });


            ArrayAdapter<Level> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Storage.INSTANCE.getLevels());
            levelSpinner.setAdapter(arrayAdapter);
            levelSpinner.setSelection(Storage.INSTANCE.getLevels().indexOf(level));
            final Level[] selectedLevel = {level};
            levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedLevel[0] = (Level) parent.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Add point")
                    .setView(view)
                    .setPositiveButton("Add", (dialogInterface, i) -> {

                        String name = inputName.getText().toString();
                        Double latitude = Double.parseDouble(inputlatitude.getText().toString());
                        Double longitude = Double.parseDouble(inputlongitude.getText().toString());
                        Double altitude = Double.parseDouble(inputAltitude.getText().toString());
                        try {
                            PointDTOs.addPointDTO addPointDTO = new PointDTOs.addPointDTO(latitude, longitude, altitude, name, selectedLevel[0].getId());
                            Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/addPoint", addPointDTO);
                            if (resp.isSuccessful()) {
                                Toast.makeText(AdminActivity4.this, resp.body().string(), Toast.LENGTH_SHORT).show();
                                refreshData();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(AdminActivity4.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        } finally {
                            LocationProvider.getInstance(null).stop();
                        }
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        LocationProvider.getInstance(null).stop();
                        dialogInterface.dismiss();
                    })
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
            level = Storage.INSTANCE.getLevels().stream().filter(l -> Objects.equals(l.getId(), level.getId())).findFirst().get();

            CustomPointAdapter adapter = new CustomPointAdapter(R.layout.admin_pointlist_item, level.getPoints(), new PointFormHandler() {
                @Override
                public void onManageConnectionClick(Point item) {
                    Intent intent = new Intent(AdminActivity4.this, AdminActivity5.class);
                    intent.putExtra("url", url);
                    intent.putExtra("levelId", level.getId() + "");
                    intent.putExtra("venue", new Gson().toJson(venue));
                    intent.putExtra("point", new Gson().toJson(item));
                    startActivity(intent);
                }

                @SuppressLint("MissingPermission")
                @Override
                public void onEditButtonClick(Point item) {

                    LayoutInflater inflater = LayoutInflater.from(AdminActivity4.this);
                    View view = inflater.inflate(R.layout.admin_point_dialog, null);

                    EditText inputName = view.findViewById(R.id.pointName);
                    Spinner levelSpinner = view.findViewById(R.id.levelSpinner);
                    EditText inputAccuracy = view.findViewById(R.id.accText);
                    EditText inputlatitude = view.findViewById(R.id.latText);
                    EditText inputlongitude = view.findViewById(R.id.lngText);
                    EditText inputAltitude = view.findViewById(R.id.altText);
                    Button measureButton = view.findViewById(R.id.measureButton);
                    AtomicBoolean isMeasuring = new AtomicBoolean(false);
                    AtomicReference<Timer> locationTimer = new AtomicReference<>(new Timer());

                    measureButton.setOnClickListener(v -> {
                        if (isMeasuring.get()) {
                            locationTimer.get().cancel();
                            locationTimer.set(new Timer());
                            measureButton.setText("MEASURE");
                            LocationProvider.getInstance(null).stop();
                            isMeasuring.set(false);

                        } else {
                            LocationProvider.getInstance(null).start();
                            locationTimer.get().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Location l = LocationProvider.getInstance(null).getCurrentLocation();
                                    if (l != null) {
                                        runOnUiThread(() -> {
                                            inputAccuracy.setText(l.getAccuracy() + "");
                                            inputlatitude.setText(l.getLatitude() + "");
                                            inputlongitude.setText(l.getLongitude() + "");
                                            inputAltitude.setText(l.getAltitude() + "");

                                            measureButton.setText("STOP");
                                        });
                                    }
                                }
                            }, 2000, 1000);
                            isMeasuring.set(true);
                        }
                    });

                    inputName.setText(item.getName());
                    inputlatitude.setText(item.getLatitude() + "");
                    inputlongitude.setText(item.getLongitude() + "");
                    inputAltitude.setText(item.getAltitude() + "");
                    inputName.setText(item.getName());
                    ArrayAdapter<Level> arrayAdapter = new ArrayAdapter<>(AdminActivity4.this, android.R.layout.simple_spinner_dropdown_item, Storage.INSTANCE.getLevels());
                    levelSpinner.setAdapter(arrayAdapter);
                    levelSpinner.setSelection(arrayAdapter.getPosition(level));
                    final Level[] selectedLevel = {level};
                    levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedLevel[0] = (Level) parent.getItemAtPosition(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                    AlertDialog dialog = new AlertDialog.Builder(AdminActivity4.this)
                            .setTitle("Edit point")
                            .setView(view)
                            .setPositiveButton("Edit", (dialogInterface, i) -> {
                                Point newpoint = new Point(
                                        item.getId(),
                                        inputName.getText().toString(),
                                        Double.parseDouble(inputlatitude.getText().toString()),
                                        Double.parseDouble(inputlongitude.getText().toString()),
                                        Double.parseDouble(inputAltitude.getText().toString())
                                );

                                try {
                                    PointDTOs.editPointDTO editPointDTO = new PointDTOs.editPointDTO(newpoint, selectedLevel[0].getId());
                                    Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/updatePoint", editPointDTO);
                                    if (resp.isSuccessful()) {
                                        Toast.makeText(AdminActivity4.this, resp.body().string(), Toast.LENGTH_SHORT).show();
                                        refreshData();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(AdminActivity4.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                } finally {
                                    LocationProvider.getInstance(null).stop();
                                }
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                LocationProvider.getInstance(null).stop();
                                dialogInterface.dismiss();
                            })
                            .create();
                    dialog.show();
                }

                @Override
                public void onRemoveButtonClick(Point item) {
                    AlertDialog dialog = new AlertDialog.Builder(AdminActivity4.this).setTitle("Confirmation").setMessage("Confirm the removal of " + item.getName() + " point").setPositiveButton(
                                    "Confirm", (dialogInterface, i) -> {
                                        try {
                                            PointDTOs.deletePointDTO deletePointDTO = new PointDTOs.deletePointDTO(item.getId());
                                            Response resp = HttpConnectionHandler.INSTANCE.doPost(url + "/admin/delPoint", deletePointDTO);
                                            if (resp.isSuccessful()) {
                                                Toast.makeText(AdminActivity4.this, resp.body().string(), Toast.LENGTH_SHORT).show();
                                                refreshData();
                                            } else
                                                Toast.makeText(AdminActivity4.this, "Something went wrong, code: " + resp.code(), Toast.LENGTH_SHORT).show();

                                        } catch (IOException e) {
                                            Toast.makeText(AdminActivity4.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create();
                    dialog.show();
                }
            }, item -> {
                //Point selected
                Intent intent = new Intent(AdminActivity4.this, MainActivity4.class);
                intent.putExtra("type", "admin");
                intent.putExtra("point", (new Gson()).toJson(item, Point.class));
                startActivity(intent);
            });
            pointList.setAdapter(adapter);

            swipeRefreshLayout.setRefreshing(false);

        } catch (IOException e) {
            Toast.makeText(AdminActivity4.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
