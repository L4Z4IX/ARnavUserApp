package com.google.ar.core.examples.java.helloar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


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

        // Get data from intent (if any)
        String receivedText = getIntent().getStringExtra("editTextInput");
        textView.setText(receivedText);

        showMotd(getIntent().getStringExtra("motdText"));





        // Click listener to go back
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish this activity and go back
                finish();
            }
        });

        itemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        venueList.setAdapter(adapter);
        //TODO venue list goes here


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
}
