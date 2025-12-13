package hu.pte.mik.l4z4ix.src.ArApplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import hu.pte.mik.l4z4ix.src.Components.dto.Login;
import hu.pte.mik.l4z4ix.src.Components.httpConnection.DataManager;

public class AdminActivity extends AppCompatActivity {
    private TextView address, name, pass;
    private final DataManager dataManager = DataManager.getManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Button backButton = findViewById(R.id.AdminBackButton);
        Button loginButton = findViewById(R.id.AdminLoginButton);
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
        dataManager.setUrl(address.getText().toString());
        Login login = new Login(name.getText().toString().trim(), pass.getText().toString().trim());
        try {
            Intent intent = new Intent(AdminActivity.this, AdminVenueActivity.class);
            intent.putExtra("name", dataManager.doLogin(login));
            startActivity(intent);
        } catch (IOException e) {
            Toast.makeText(AdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
