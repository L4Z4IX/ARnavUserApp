package hu.pte.mik.l4z4ix.src.helloar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import hu.pte.mik.l4z4ix.src.common.helpers.CameraPermissionHelper;
import hu.pte.mik.l4z4ix.src.common.helpers.LocationPermissionHelper;
import hu.pte.mik.l4z4ix.src.common.httpConnection.HttpConnectionHandler;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView clickableText;
    private EditText editTextInput;
    private Button submitButton;
    private Button adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clickableText = findViewById(R.id.clickableText);
        editTextInput = findViewById(R.id.editTextInput);
        submitButton = findViewById(R.id.submitButton);
        adminButton = findViewById(R.id.adminButton);

        clickableText.setOnClickListener(v -> {
            showPopup();
        });
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        submitButton.setOnClickListener(v -> {
            String inputText = editTextInput.getText().toString().trim();
            if (!inputText.isEmpty()) {
                Response resp = null;
                try {
                    resp = HttpConnectionHandler.INSTANCE.newRequest("http://" + inputText + "/data/hello");
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Invalid address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (resp == null || !resp.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] data;
                try {
                    data = HttpConnectionHandler.INSTANCE.getResponseString(resp).split(";");
                    if (data.length != 3) {
                        Toast.makeText(MainActivity.this, "Address does not use correct configuration", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    return;
                }
                // Navigate to SecondActivity
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("editTextInput", data[0].trim());
                intent.putExtra("motdText", data[2].trim());
                intent.putExtra("url", inputText);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Please enter an address", Toast.LENGTH_SHORT).show();
            }
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
