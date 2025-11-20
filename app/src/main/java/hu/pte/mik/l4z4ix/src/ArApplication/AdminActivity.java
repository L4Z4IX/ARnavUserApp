package hu.pte.mik.l4z4ix.src.ArApplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import hu.pte.mik.l4z4ix.src.Components.dto.Login;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.HttpConnectionHandler;
import okhttp3.Response;

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
            String url = "http://" + address.getText().toString();
            Login login = new Login(name.getText().toString().trim(), pass.getText().toString().trim());

            try (Response response = HttpConnectionHandler.INSTANCE.doPost(url + "/login", login)) {
                if (!response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, response.code() + ": Invalid creditentials", Toast.LENGTH_SHORT).show();
                } else {
                    String[] data = HttpConnectionHandler.INSTANCE.getResponseString(
                            HttpConnectionHandler.INSTANCE.newRequest(url + "/data/hello")
                    ).split(";");
                    Intent intent = new Intent(AdminActivity.this, AdminActivity2.class);
                    intent.putExtra("url", url);
                    intent.putExtra("name", data[0]);
                    startActivity(intent);
                }
            }


        } catch (IOException e) {
            Toast.makeText(AdminActivity.this, "Invalid address", Toast.LENGTH_SHORT).show();
        }
    }
}
