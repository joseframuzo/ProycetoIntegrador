package com.example.servicios.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.example.servicios.R;
import com.example.servicios.api.ApiClient;
import com.example.servicios.api.ApiService;
import com.example.servicios.data.SessionManager;
import com.example.servicios.models.*;

import java.util.*;

import retrofit2.*;

public class DetailActivity extends AppCompatActivity {
    TextView txtName, txtHeadline, txtAbout, txtNotes, txtExp, txtContact;
    RecyclerView rvPhotos;
    PhotoAdapter photoAdapter;
    ApiService api;
    SessionManager session;
    String profId;
    ProfessionalDetail detail;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_detail);
        session = new SessionManager(this);
        api = ApiClient.get().create(ApiService.class);

        txtName = findViewById(R.id.txtName);
        txtHeadline = findViewById(R.id.txtHeadline);
        txtAbout = findViewById(R.id.txtAbout);
        txtNotes = findViewById(R.id.txtNotes);
        txtExp = findViewById(R.id.txtExp);
        txtContact = findViewById(R.id.txtContact);


        rvPhotos = findViewById(R.id.rvPhotos);
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(new ArrayList<>());
        rvPhotos.setAdapter(photoAdapter);

        profId = getIntent().getStringExtra("PROF_ID");
        load();
        txtContact.setOnClickListener(v -> openChat());
    }

    void load() {
        api.professionalDetail(profId).enqueue(new Callback<ProfessionalDetail>() {
            @Override public void onResponse(Call<ProfessionalDetail> call, Response<ProfessionalDetail> r) {
                if (r.isSuccessful() && r.body()!=null) {
                    detail = r.body();
                    String name = detail.AppUser!=null ? detail.AppUser.full_name : "";
                    txtName.setText(name);
                    txtHeadline.setText(detail.headline!=null? detail.headline : detail.main_category);
                    txtAbout.setText(detail.about!=null? detail.about : "");
                    txtNotes.setText(detail.notes!=null? detail.notes : "");
                    txtExp.setText(detail.experience_years!=null? (detail.experience_years+" años de experiencia") : "");

                    List<Photo> ph = detail.photos != null ? detail.photos : detail.ProfessionalPhotos;
                    if (ph == null) ph = new ArrayList<>();
                    photoAdapter.setData(ph);
                } else {
                    Toast.makeText(DetailActivity.this, "No encontrado", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ProfessionalDetail> call, Throwable t) {
                Toast.makeText(DetailActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void openChat() {
        if (detail == null || session.userId()==null) {
            Toast.makeText(this, "Inicia sesión", Toast.LENGTH_SHORT).show();
            return;
        }
        // Abrir/crear conversación
        Map<String, String> b = new HashMap<>();
        b.put("from_user_id", session.userId());
        b.put("to_user_id", detail.user_id);
        api.openConversation(b).enqueue(new Callback<OpenConversationResponse>() {
            @Override public void onResponse(Call<OpenConversationResponse> call, Response<OpenConversationResponse> r) {
                if (r.isSuccessful() && r.body()!=null) {
                    Intent i = new Intent(DetailActivity.this, ChatActivity.class);
                    i.putExtra("CONV_ID", r.body().conversation_id);
                    startActivity(i);
                } else Toast.makeText(DetailActivity.this, "No se pudo abrir chat", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<OpenConversationResponse> call, Throwable t) {
                Toast.makeText(DetailActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.VH> {
        List<Photo> data;
        PhotoAdapter(List<Photo> d){ data=d; }
        void setData(List<Photo> d){ data=d; notifyDataSetChanged(); }

        @Override public VH onCreateViewHolder(ViewGroup p, int v) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_photo, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            Photo ph = data.get(pos);
            Glide.with(h.img.getContext()).load(ph.url).into(h.img);
        }
        @Override public int getItemCount(){ return data==null?0:data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            ImageView img; VH(View v){ super(v); img = v.findViewById(R.id.imgPhoto); }
        }
    }
}
