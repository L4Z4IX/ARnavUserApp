package hu.pte.mik.l4z4ix.src.ArApplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import java.io.IOException;

import hu.pte.mik.l4z4ix.src.Components.dto.ConnectionDTOs;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Point;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Venue;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.DataManager;
import hu.pte.mik.l4z4ix.src.Components.listHelpers.CustomConnectionAdapter;
import okhttp3.Response;

public class AdminActivity5 extends AppCompatActivity {

    private Point point;
    private Venue venue;
    private RecyclerView connectionList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final DataManager dataManager = DataManager.getManager();

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

        venue = new Gson().fromJson(getIntent().getStringExtra("venue"), Venue.class);
        point = new Gson().fromJson(getIntent().getStringExtra("point"), Point.class);

        pointName.setText(point.getName());

        connectionList.setLayoutManager(new LinearLayoutManager(this));
        refreshData();


        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);
        try {
            dataManager.requestConnectionsByVenue(venue);
            CustomConnectionAdapter connectionAdapter = new CustomConnectionAdapter(Storage.INSTANCE.getLevels().stream().flatMap(x -> x.getPoints().stream()).toList(), Storage.INSTANCE.getConnections(), (item, state) -> {
                Response res;
                try {
                    if (state) {
                        ConnectionDTOs.addConnectionDTO addConnectionDTO = new ConnectionDTOs.addConnectionDTO(point.getId(), item.getId());
                        Toast.makeText(AdminActivity5.this, dataManager.addConnection(addConnectionDTO), Toast.LENGTH_SHORT).show();
                    } else {
                        ConnectionDTOs.delConnectionDTO delConnectionDTO = new ConnectionDTOs.delConnectionDTO(point.getId(), item.getId());
                        Toast.makeText(AdminActivity5.this, dataManager.deleteConnection(delConnectionDTO), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(AdminActivity5.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, point);
            connectionList.setAdapter(connectionAdapter);


        } catch (IOException e) {
            Toast.makeText(AdminActivity5.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
