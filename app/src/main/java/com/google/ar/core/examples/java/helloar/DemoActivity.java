package com.google.ar.core.examples.java.helloar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.navigation.LocationProvider;

import java.util.Timer;
import java.util.TimerTask;

public class DemoActivity extends AppCompatActivity {
    private final Timer locationTimer = new Timer();
    private TextView accuracyTextView;
    private TextView measure;
    private Button measureButton;
    private boolean measureing = false;
    private boolean measured = false;
    private EditText testLocationField;
    private Button startButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("started");
        setContentView(R.layout.demoactivity);
        accuracyTextView = findViewById(R.id.accField);
        measure = findViewById(R.id.measure);
        measureButton = findViewById(R.id.measureButton);
        testLocationField = findViewById(R.id.testLocationField);
        startButton = findViewById(R.id.startButton);

        measure.setOnClickListener(v -> {
                    if (measureing)
                        return;
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", measure.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
        );

        measureButton.setOnClickListener(v -> {
            if (measureing) {
                locationTimer.cancel();
                locationTimer.purge();
                measureButton.setText("Start measurement");

            } else {
                measured = true;
                locationTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Location currentLocation = LocationProvider.getInstance(null).getCurrentLocation();
                        runOnUiThread(() -> {
                            accuracyTextView.setText(currentLocation.getAccuracy() + "");
                            measure.setText(currentLocation.getAltitude() + ";" + currentLocation.getLatitude() + ";" + currentLocation.getLongitude());
                        });
                    }
                }, 3000, 1000);
                measureButton.setText("Stop measurement");
            }
            measureing = !measureing;
        });

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(DemoActivity.this, MainActivity4.class);
            intent.putExtra("location", testLocationField.getText() + "");
            startActivity(intent);
        });

    }

    @Override
    protected void onDestroy() {
        locationTimer.purge();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        locationTimer.purge();
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }
}
