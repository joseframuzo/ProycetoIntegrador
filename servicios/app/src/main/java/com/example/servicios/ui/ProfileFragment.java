

package com.example.servicios.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.servicios.R;
import com.example.servicios.api.ApiClient;
import com.example.servicios.api.ApiService;
import com.example.servicios.data.SessionManager;
import com.example.servicios.models.Photo;
import com.example.servicios.models.ProfessionalDetail;

import java.util.*;
import retrofit2.*;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.ArrayAdapter;

public class ProfileFragment extends Fragment {

    EditText edtNotes, edtExp, edtHeadline, edtAbout, edtCity, edtPhotoUrl;
    MaterialAutoCompleteTextView edtMainCategory; // <--- NUEVO
    Button btnSave, btnAddPhoto, btnLogout;
    TextView txtInfo;
    SessionManager session;
    ApiService api;

    // Nombre visible -> slug BD
    private static final java.util.Map<String, String> CAT_MAP = new java.util.HashMap<String, String>() {{
        put("Arquitecto", "arquitecto");
        put("Plomero", "plomero");
        put("Pintor", "pintor");
        put("Albañil", "albanil"); // sin ñ en slug
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_profile, c, false);
        session = new SessionManager(requireContext());
        api = ApiClient.get().create(ApiService.class);

        edtNotes = v.findViewById(R.id.edtNotes);
        edtExp = v.findViewById(R.id.edtExp);
        edtHeadline = v.findViewById(R.id.edtHeadline);
        edtAbout = v.findViewById(R.id.edtAbout);
        edtCity = v.findViewById(R.id.edtCity);
        edtPhotoUrl = v.findViewById(R.id.edtPhotoUrl);
        edtMainCategory = v.findViewById(R.id.edtMainCategory); // <--- NUEVO
        btnSave = v.findViewById(R.id.btnSave);
        btnAddPhoto = v.findViewById(R.id.btnAddPhoto);
        btnLogout = v.findViewById(R.id.btnLogout);
        txtInfo = v.findViewById(R.id.txtInfo);

        // Poblamos el dropdown de categorías
        java.util.List<String> cats = java.util.Arrays.asList("Arquitecto", "Plomero", "Pintor", "Albañil");
        edtMainCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, cats));

        btnSave.setOnClickListener(x -> save());
        btnAddPhoto.setOnClickListener(x -> addPhoto());
        btnLogout.setOnClickListener(x -> {
            session.logout();
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        txtInfo.setText( session.role() );
        return v;
    }

    void save() {
        if (!"PROFESSIONAL".equals(session.role())) {
            Toast.makeText(getContext(), "Solo profesional puede editar perfil", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", session.userId());

        // main_category (slug) — si el usuario eligió algo
        String visible = edtMainCategory.getText() == null ? "" : edtMainCategory.getText().toString().trim();
        if (!visible.isEmpty()) {
            String slug = CAT_MAP.get(visible); // null si no está en el mapa
            if (slug != null) {
                body.put("main_category", slug);
            } else {
                Toast.makeText(getContext(), "Categoría no válida", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!edtHeadline.getText().toString().trim().isEmpty())
            body.put("headline", edtHeadline.getText().toString().trim());
        if (!edtAbout.getText().toString().trim().isEmpty())
            body.put("about", edtAbout.getText().toString().trim());
        if (!edtCity.getText().toString().trim().isEmpty())
            body.put("city", edtCity.getText().toString().trim());
        if (!edtExp.getText().toString().trim().isEmpty()) {
            try {
                body.put("experience_years", Integer.parseInt(edtExp.getText().toString().trim()));
            } catch (NumberFormatException nfe) {
                Toast.makeText(getContext(), "Años de experiencia inválido", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!edtNotes.getText().toString().trim().isEmpty())
            body.put("notes", edtNotes.getText().toString().trim());

        api.saveNotes(body).enqueue(new Callback<ProfessionalDetail>() {
            @Override public void onResponse(Call<ProfessionalDetail> call, Response<ProfessionalDetail> r) {
                Toast.makeText(getContext(), r.isSuccessful() ? "Guardado" : "Error guardando", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<ProfessionalDetail> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void addPhoto() {
        if (!"PROFESSIONAL".equals(session.role())) {
            Toast.makeText(getContext(), "Solo profesional puede agregar foto", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = edtPhotoUrl.getText().toString().trim();
        if (url.isEmpty()) { Toast.makeText(getContext(), "URL requerida", Toast.LENGTH_SHORT).show(); return; }

        Map<String, String> body = new HashMap<>();
        body.put("user_id", session.userId());
        body.put("url", url);

        api.addPhoto(body).enqueue(new Callback<Photo>() {
            @Override public void onResponse(Call<Photo> call, Response<Photo> r) {
                Toast.makeText(getContext(), r.isSuccessful() ? "Foto agregada" : "Error", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<Photo> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
