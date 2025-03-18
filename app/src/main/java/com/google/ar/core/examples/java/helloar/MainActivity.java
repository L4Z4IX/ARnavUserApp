package com.google.ar.core.examples.java.helloar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.httpConnection.HttpConnectionHandler;

import java.io.IOException;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView clickableText;
    private EditText editTextInput;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        clickableText = findViewById(R.id.clickableText);
        editTextInput = findViewById(R.id.editTextInput);
        submitButton = findViewById(R.id.submitButton);

        // Set click listener for the clickable text (popup dialog)
        clickableText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });

        // Set click listener for the submit button
        submitButton.setOnClickListener(v -> {
            String inputText = editTextInput.getText().toString().trim();
            if (!inputText.isEmpty()) {
                Response resp = null;
                try {
                    resp= HttpConnectionHandler.INSTANCE.newRequest("http://"+inputText+"/hello");
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Invalid address",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(resp==null||!resp.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Invalid response from server",Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] data;
                try {
                    data=HttpConnectionHandler.INSTANCE.getResponseString(resp).split(";");
                    if(data.length!=3){
                        Toast.makeText(MainActivity.this, "Address does not use correct configuration",Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    return;
                }
                // Navigate to SecondActivity
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("editTextInput",data[0].trim());
                intent.putExtra("motdText",data[2].trim());
                intent.putExtra("url",inputText);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Please enter an address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to show a popup dialog
    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Where can I find the address?")
                .setMessage("The address is usually the same as the organisation's web address. Check out their page for more info.")
                .setPositiveButton("OK", null)
                .show();
    }
}
