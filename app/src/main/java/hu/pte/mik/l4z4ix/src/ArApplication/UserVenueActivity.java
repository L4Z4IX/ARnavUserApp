package hu.pte.mik.l4z4ix.src.ArApplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;

import hu.pte.mik.l4z4ix.src.Components.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Venue;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.DataManager;


public class UserVenueActivity extends AppCompatActivity {
    private ListView venueList;
    private ArrayAdapter<Venue> adapter;
    private final DataManager dataManager = DataManager.getManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_venue);

        TextView textView = findViewById(R.id.textView);
        Button backButton = findViewById(R.id.backButton);
        venueList = findViewById(R.id.venueList);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        String receivedText = getIntent().getStringExtra("editTextInput");
        textView.setText(receivedText);

        showMotd(getIntent().getStringExtra("motdText"));


        backButton.setOnClickListener(v -> {
            finish();
        });
        swipeRefreshLayout.setOnRefreshListener(() -> populateList(swipeRefreshLayout));
        populateList(swipeRefreshLayout);

    }


    private void showMotd(String motd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message of the day")
                .setMessage(motd)
                .setPositiveButton("Got it!", null)
                .show();
    }

    private void populateList(SwipeRefreshLayout swipeRefreshLayout) {
        try {
            dataManager.requestVenues();
        } catch (IOException e) {
            Toast.makeText(UserVenueActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Storage.INSTANCE.getVenues());
        venueList.setAdapter(adapter);
        venueList.setOnItemClickListener((adapter, v, position, id) -> {
            itemSelected(position);
        });
        Toast.makeText(UserVenueActivity.this, "Venues loaded", Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void itemSelected(int position) {
        Intent intent = new Intent(UserVenueActivity.this, UserPointActivity.class);
        intent.putExtra("venueId", position + "");
        startActivity(intent);
    }
}
