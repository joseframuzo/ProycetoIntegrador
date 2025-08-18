package com.pintegrador.servicios.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // Campos comunes
    private EditText etName, etEmail, etPhone, etPass;

    // Campos extra para profesional
    private EditText etCedula, etHeadline, etAbout, etCity;
    private Spinner spCategory;
    private View proSection;        // contenedor de los campos de profesional
    private Button btnCreate;

    private String role = "USER";   // viene de LoginActivity (USER o PROFESSIONAL)

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // --- Binds (asegúrate que estos ids existan en tu XML) ---
        etName    = findViewById(R.id.etName);
        etEmail   = findViewById(R.id.etEmail);
        etPhone   = findViewById(R.id.etPhone);
        etPass    = findViewById(R.id.etPass);

        etCedula  = findViewById(R.id.etCedula);
        spCategory= findViewById(R.id.spCategory);
        etHeadline= findViewById(R.id.etHeadline);
        etAbout   = findViewById(R.id.etAbout);
        etCity    = findViewById(R.id.etCity);

        proSection= findViewById(R.id.proSection); // envuelve los campos de profesional
        btnCreate = findViewById(R.id.btnCreate);

        // Role desde LoginActivity
        String r = getIntent().getStringExtra("role");
        if (r != null) role = r.toUpperCase();

        // Spinner de categorías visibles para profesional (4 fijas)
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Arquitecto", "Plomero", "Pintor", "Albañil"}
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // Mostrar/ocultar sección profesional
        proSection.setVisibility("PROFESSIONAL".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);

        btnCreate.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String fullName = etName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim().toLowerCase();
        String phone    = etPhone.getText().toString().trim();
        String pass     = etPass.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            toast("Completa nombre, email y contraseña");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("password", pass);
        body.put("role", role);

        if ("PROFESSIONAL".equalsIgnoreCase(role)) {
            String cedula   = etCedula.getText().toString().trim();
            String catText  = String.valueOf(spCategory.getSelectedItem()); // ej. "Arquitecto"
            String mainCat  = slug(catText);                                // "arquitecto"

            // Validar que sea una de las 4 permitidas
            List<String> allowed = Arrays.asList("arquitecto", "plomero", "pintor", "albanil");
            if (!allowed.contains(mainCat)) {
                toast("Selecciona una categoría válida");
                return;
            }

            String headline = etHeadline.getText().toString().trim();
            String about    = etAbout.getText().toString().trim();
            String city     = etCity.getText().toString().trim();

            body.put("cedula", cedula);
            body.put("main_category", mainCat);
            if (!headline.isEmpty()) body.put("headline", headline);
            if (!about.isEmpty())    body.put("about", about);
            if (!city.isEmpty())     body.put("city", city);
        }

        btnCreate.setEnabled(false);

        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.register(body).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                btnCreate.setEnabled(true);
                if (res.isSuccessful() && res.body() != null) {
                    LoginResponse lr = res.body();
                    Prefs.saveAuth(RegisterActivity.this, lr.token, lr.user.role, lr.user.full_name);

                    Intent i;
                    if ("PROFESSIONAL".equalsIgnoreCase(lr.user.role)) {
                        i = new Intent(RegisterActivity.this, ProfessionalHomeActivity.class);
                    } else {
                        i = new Intent(RegisterActivity.this, UserHomeActivity.class);
                    }
                    startActivity(i);
                    finish();
                } else {
                    toast(parseError(res));
                }
            }

            @Override public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnCreate.setEnabled(true);
                toast("Error: " + t.getMessage());
            }
        });
    }

    /** Normaliza a slug: sin acentos, minúsculas, sin espacios finales. */
    private String slug(String text) {
        if (text == null) return "";
        String n = Normalizer.normalize(text, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}+", "");   // quita acentos/diacríticos
        n = n.toLowerCase().trim();       // "arquitecto", "albañil" -> "albañil"
        if (n.equals("albañil")) n = "albanil"; // backend espera "albanil" sin ñ
        return n;
    }

    /** Lee el mensaje { "error": "..." } del backend si viene; si no, texto genérico. */
    private String parseError(Response<?> res) {
        try {
            if (res.errorBody() != null) {
                String s = res.errorBody().string();
                org.json.JSONObject o = new org.json.JSONObject(s);
                if (o.has("error")) return o.getString("error");
            }
        } catch (Exception ignored) {}
        return "Error al registrar";
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
