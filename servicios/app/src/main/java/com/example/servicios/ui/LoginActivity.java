package com.example.servicios.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.servicios.R;
import com.example.servicios.api.ApiClient;
import com.example.servicios.api.ApiService;
import com.example.servicios.data.SessionManager;
import com.example.servicios.models.User;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPass;
    private MaterialButton btnLogin, btnRegister;
    private ProgressBar progress;

    private SessionManager session;
    private ApiService api;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login); // tu XML

        session = new SessionManager(this);
        if (session.isLogged()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        api = ApiClient.get().create(ApiService.class);

        edtEmail   = findViewById(R.id.edtEmail);
        edtPass    = findViewById(R.id.edtPass);
        btnLogin   = findViewById(R.id.btnLogin);
        btnRegister= findViewById(R.id.btnRegister);
        progress   = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> doLogin());
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void doLogin() {
        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
        String pass  = edtPass.getText()  == null ? "" : edtPass.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email y contraseña requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);

        Map<String,String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", pass);

        api.login(body).enqueue(new Callback<User>() {
            @Override public void onResponse(Call<User> call, Response<User> r) {
                progress.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body()!=null) {
                    session.save(r.body());
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
