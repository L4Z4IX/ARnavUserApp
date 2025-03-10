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
                // Navigate to SecondActivity
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("editTextInput",inputText);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
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
