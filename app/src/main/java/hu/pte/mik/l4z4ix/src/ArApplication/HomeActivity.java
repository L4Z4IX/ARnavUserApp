package hu.pte.mik.l4z4ix.src.ArApplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import hu.pte.mik.l4z4ix.src.Components.helpers.CameraPermissionHelper;
import hu.pte.mik.l4z4ix.src.Components.helpers.LocationPermissionHelper;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.DataManager;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.HttpConnectionHandler;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView clickableText;
    private EditText editTextInput;
    private Button submitButton;
    private Button adminButton;
    private final DataManager dataManager = DataManager.getManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        clickableText = findViewById(R.id.clickableText);
        editTextInput = findViewById(R.id.editTextInput);
        submitButton = findViewById(R.id.submitButton);
        adminButton = findViewById(R.id.adminButton);

        clickableText.setOnClickListener(v -> {
            showPopup();
        });
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        submitButton.setOnClickListener(v -> {
            String inputText = editTextInput.getText().toString().trim();
            String[] data;
            if (!inputText.isEmpty()) {
                dataManager.setUrl(inputText);
                Response resp;
                try {
                    resp = dataManager.doHello();
                } catch (IOException e) {
                    Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    String respString = HttpConnectionHandler.getInstance().getResponseString(resp);
                    if (respString == null || respString.split(";").length != 3) {
                        throw new IOException();
                    }
                    data = respString.split(";");
                } catch (IOException e) {
                    Toast.makeText(HomeActivity.this, "Address does not use correct configuration", Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                Toast.makeText(HomeActivity.this, "Address can not be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(HomeActivity.this, UserVenueActivity.class);
            intent.putExtra("editTextInput", data[0].trim());
            intent.putExtra("motdText", data[2].trim());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }
        if (!LocationPermissionHelper.hasFineLocationPermission(this)) {
            LocationPermissionHelper.requestFineLocationPermission(this);
        }
    }

    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Where can I find the address?")
                .setMessage("The address is usually the same as the organisation's web address. Check out their page for more info.")
                .setPositiveButton("OK", null)
                .show();
    }
}
