package com.google.ar.core.examples.java.helloar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.httpConnection.HttpConnectionHandler;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class AdminActivity extends AppCompatActivity {
    Button backButton, loginButton;
    TextView address, name, pass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        backButton = findViewById(R.id.AdminBackButton);
        loginButton = findViewById(R.id.AdminLoginButton);
        address = findViewById(R.id.AdminServerAddr);
        name = findViewById(R.id.AdminUsername);
        pass = findViewById(R.id.AdminPass);

        backButton.setOnClickListener(v -> {
            finish();
        });
        loginButton.setOnClickListener(v -> {
            onLogin();
        });
    }

    private void onLogin() {
        try {
            RequestBody requestBody = new RequestBody() {
                @Nullable
                @Override
                public MediaType contentType() {
                    return null;
                }

                @Override
                public void writeTo(@NonNull BufferedSink bufferedSink) throws IOException {

                }
            };
            try (Response response = HttpConnectionHandler.INSTANCE.doPost(
                    "http://" + address.getText().toString() + "/login?" +
                            "username=" + name.getText().toString() + "&password=" + pass.getText().toString(),
                    requestBody)) {
                if (!response.isSuccessful()) {
                    System.out.println("resp code: " + response.code());
                    Toast.makeText(AdminActivity.this, "Invalid creditentials", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO successful login
                    Toast.makeText(AdminActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
                }
            }


        } catch (IOException e) {
            Toast.makeText(AdminActivity.this, "Invalid address", Toast.LENGTH_SHORT).show();
        }
    }
}
