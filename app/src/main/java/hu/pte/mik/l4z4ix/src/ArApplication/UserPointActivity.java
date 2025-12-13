package hu.pte.mik.l4z4ix.src.ArApplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;

import hu.pte.mik.l4z4ix.src.Components.entityModel.Point;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.DataManager;

public class UserPointActivity extends AppCompatActivity {
    private ArrayAdapter<Point> adapter;
    private ListView pointList;
    private Button backButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String venueId;
    private final DataManager dataManager = DataManager.getManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_point);
        TextInputLayout searchBar = findViewById(R.id.textInputLayout);
        pointList = findViewById(R.id.pointList);
        backButton = findViewById(R.id.backButton2);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


        backButton.setOnClickListener(v -> {
            finish();
        });
        searchBar.getEditText()
                .addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                            }

                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                populateList(s);
                                            }

                                            @Override
                                            public void afterTextChanged(Editable s) {

                                            }
                                        }
                );
        String url = getIntent().getStringExtra("url");
        venueId = getIntent().getStringExtra("venueId");
        ((TextView) findViewById(R.id.textView3)).setText(Storage.INSTANCE.getVenues().get(Integer.parseInt(venueId)).getName());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            searchBar.getEditText().setText("");
            getData(venueId);
        });

        getData(venueId);
    }

    private void populateList(CharSequence filter) {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                Storage.INSTANCE.getLevels().stream()
                        .flatMap(x -> x.getPoints().stream().filter(y -> y.getName().contains(filter))).toList());
        pointList.setAdapter(adapter);
        pointList.setOnItemClickListener((adapter, v, position, id) -> itemSelected((AdapterView<ArrayAdapter<Point>>) adapter, position));
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getData(String venueIndex) {
        try {
            dataManager.requestVenueData(Integer.parseInt(venueIndex));
            Toast.makeText(UserPointActivity.this, "Points loaded", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(UserPointActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        populateList("");
    }

    private void itemSelected(AdapterView<ArrayAdapter<Point>> adapter, int position) {
        Intent intent = new Intent(UserPointActivity.this, ArActivity.class);
        intent.putExtra("point", (new Gson()).toJson(adapter.getItemAtPosition(position), Point.class));
        intent.putExtra("venueId", venueId);
        intent.putExtra("type", "user");
        startActivity(intent);
    }
}
