package com.pintegrador.servicios.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pintegrador.servicios.R;
import com.pintegrador.servicios.network.ApiClient;
import com.pintegrador.servicios.network.ApiService;
import com.pintegrador.servicios.network.dto.ProfessionalProfile;
import com.pintegrador.servicios.network.dto.Review;
import com.pintegrador.servicios.util.Prefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfessionalHomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvAbout, tvHeadline, tvCategory, tvCity, tvPhone, tvRatingSummary;
    private RecyclerView rvPhotos, rvReviews;
    private PhotosAdapter photosAdapter;
    private ReviewsAdapter reviewsAdapter;
    private View boxReviewForm;
    private RatingBar rbMyRating; private EditText etMyComment; private Button btnSendReview, btnBack, btnHome;
    private ProgressBar pb;

    private String myRole = "USER";
    private String professionalId; // para reviews

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_home);

        // Toolbar back
        findViewById(R.id.toolbar).setOnClickListener(v -> onBackPressed());

        myRole = Prefs.getRole(this) != null ? Prefs.getRole(this) : "USER";

        tvWelcome = findViewById(R.id.tvWelcome);
        tvAbout = findViewById(R.id.tvAbout);
        tvHeadline = findViewById(R.id.tvHeadline);
        tvCategory = findViewById(R.id.tvCategory);
        tvCity = findViewById(R.id.tvCity);
        tvPhone = findViewById(R.id.tvPhone);
        tvRatingSummary = findViewById(R.id.tvRatingSummary);
        pb = findViewById(R.id.pb);

        rvPhotos = findViewById(R.id.rvPhotos);
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        photosAdapter = new PhotosAdapter();
        rvPhotos.setAdapter(photosAdapter);

        rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewsAdapter = new ReviewsAdapter();
        rvReviews.setAdapter(reviewsAdapter);

        boxReviewForm = findViewById(R.id.boxReviewForm);
        rbMyRating = findViewById(R.id.rbMyRating);
        etMyComment = findViewById(R.id.etMyComment);
        btnSendReview = findViewById(R.id.btnSendReview);
        btnBack = findViewById(R.id.btnBack);
        btnHome = findViewById(R.id.btnHome);

        // Navegación
        btnBack.setOnClickListener(v -> onBackPressed());
        btnHome.setOnClickListener(v -> {
            Intent i = "PROFESSIONAL".equalsIgnoreCase(myRole)
                    ? new Intent(this, ProfessionalHomeActivity.class)
                    : new Intent(this, UserHomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        // El formulario solo lo ve el USUARIO
        boxReviewForm.setVisibility("USER".equalsIgnoreCase(myRole) ? View.VISIBLE : View.GONE);

        btnSendReview.setOnClickListener(v -> sendReview());

        loadProfile();
    }

    private void loadProfile() {
        pb.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.get(this).create(ApiService.class);

        // Si quieres cargar siempre el perfil del que inició sesión
        api.getMyProfile().enqueue(new Callback<ProfessionalProfile>() {
            @Override public void onResponse(Call<ProfessionalProfile> call, Response<ProfessionalProfile> res) {
                pb.setVisibility(View.GONE);
                if (res.isSuccessful() && res.body()!=null) {
                    bind(res.body());
                } else {
                    Toast.makeText(ProfessionalHomeActivity.this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ProfessionalProfile> call, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(ProfessionalHomeActivity.this, "Error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bind(ProfessionalProfile p){
        professionalId = p.id;
        tvWelcome.setText("Hola, " + (p.full_name == null ? "" : p.full_name));
        tvAbout.setText(p.about == null ? "Sin descripción" : p.about);
        tvHeadline.setText(p.headline == null ? "" : p.headline);
        tvCategory.setText("Categoría: " + p.main_category);
        tvCity.setText("Ciudad: " + (p.city == null ? "-" : p.city));
        tvPhone.setText("Teléfono: " + (p.phone == null ? "-" : p.phone));
        tvRatingSummary.setText(String.format("%.1f ★ (%d)", p.rating_avg, p.rating_count));

        photosAdapter.setData(p.photos);
        reviewsAdapter.setData(p.reviews);
    }

    private void sendReview(){
        if (professionalId == null) {
            Toast.makeText(this, "Perfil no cargado", Toast.LENGTH_SHORT).show();
            return;
        }
        int stars = Math.round(rbMyRating.getRating());
        String comment = etMyComment.getText().toString().trim();
        if (stars <= 0) { Toast.makeText(this, "Elige una puntuación", Toast.LENGTH_SHORT).show(); return; }

        Review r = new Review();
        r.rating = stars;
        r.comment = comment;

        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.postReview(professionalId, r).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {
                    Toast.makeText(ProfessionalHomeActivity.this, "¡Gracias por tu valoración!", Toast.LENGTH_SHORT).show();
                    etMyComment.setText(""); rbMyRating.setRating(0);
                    loadProfile(); // recarga lista y promedio
                } else {
                    Toast.makeText(ProfessionalHomeActivity.this, "No se pudo enviar la valoración", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfessionalHomeActivity.this, "Error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
