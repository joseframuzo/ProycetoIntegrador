package com.pintegrador.servicios.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pintegrador.servicios.R;
import com.pintegrador.servicios.network.ApiClient;
import com.pintegrador.servicios.network.ApiService;
import com.pintegrador.servicios.network.dto.LoginResponse;
import com.pintegrador.servicios.util.Prefs;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPass;
    private Spinner spRole; // solo decide a qué registro ir
    private Button btnLogin, btnRegister;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        spRole = findViewById(R.id.spRole);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        ArrayAdapter<String> roles = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Usuario","Profesional"}
        );
        roles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(roles);

        btnRegister.setOnClickListener(v -> {
            String selected = spRole.getSelectedItem().toString();
            Intent i = new Intent(this, RegisterActivity.class);
            i.putExtra("role", selected.equals("Profesional") ? "PROFESSIONAL" : "USER");
            startActivity(i);
        });

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()){
            Toast.makeText(this, "Email y contraseña son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.get(this).create(ApiService.class);
        Map<String,Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", pass);

        btnLogin.setEnabled(false);
        api.login(body).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                btnLogin.setEnabled(true);
                if (res.isSuccessful() && res.body()!=null){
                    Prefs.saveAuth(LoginActivity.this, res.body().token, res.body().user.role, res.body().user.full_name);

                    if ("PROFESSIONAL".equalsIgnoreCase(res.body().user.role)) {
                        startActivity(new Intent(LoginActivity.this, ProfessionalHomeActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, UserHomeActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
