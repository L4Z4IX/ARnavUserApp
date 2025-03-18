package com.google.ar.core.examples.java.helloar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.ar.core.examples.java.common.entityModel.Storage;
import com.google.ar.core.examples.java.common.entityModel.Venue;
import com.google.ar.core.examples.java.common.httpConnection.HttpConnectionHandler;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Response;


public class MainActivity2 extends AppCompatActivity {
    private ListView venueList;
    private ArrayList<String> itemList;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        TextView textView = findViewById(R.id.textView);
        Button backButton = findViewById(R.id.backButton);
        venueList=findViewById(R.id.venueList);
        SwipeRefreshLayout swipeRefreshLayout=findViewById(R.id.swipeRefreshLayout);

        // Get data from intent (if any)
        String receivedText = getIntent().getStringExtra("editTextInput");
        textView.setText(receivedText);

        showMotd(getIntent().getStringExtra("motdText"));
        String url=getIntent().getStringExtra("url");





        // Click listener to go back
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish this activity and go back
                finish();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(()->populateList(url,swipeRefreshLayout));



        populateList(url,swipeRefreshLayout);

    }
    private void addItem(String item) {
        itemList.add(item);
        adapter.notifyDataSetChanged(); // Refresh ListView
    }
    private void showMotd(String motd){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message of the day")
                .setMessage(motd)
                .setPositiveButton("Got it!", null)
                .show();
    }
    private void populateList(String url,SwipeRefreshLayout swipeRefreshLayout){
        Storage.INSTANCE.clearInstance();
        try {
            Response r= HttpConnectionHandler.INSTANCE.newRequest("http://"+url+"/data/venues");
            if(!r.isSuccessful()){
                throw new IOException();
            }
            Storage.INSTANCE.setVenues(HttpConnectionHandler.INSTANCE.getResponseFromJson(r,Venue.LIST_TYPE_TOKEN));

        } catch (IOException e) {
            Toast.makeText(MainActivity2.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Storage.INSTANCE.getVenues().stream().map(x->x.getName()).toList());
        venueList.setAdapter(adapter);
        Toast.makeText(MainActivity2.this,"Refreshed",Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
    }
}
