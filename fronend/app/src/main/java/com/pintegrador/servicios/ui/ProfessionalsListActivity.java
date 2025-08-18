package com.pintegrador.servicios.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.pintegrador.servicios.R;
import com.pintegrador.servicios.network.ApiClient;
import com.pintegrador.servicios.network.ApiService;
import com.pintegrador.servicios.network.dto.Professional;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfessionalsListActivity extends AppCompatActivity {
    private ListView list;
    private ProgressBar pb;
    private String category;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professionals_list);
        list = findViewById(R.id.listPros);
        pb = findViewById(R.id.pb);
        category = getIntent().getStringExtra("category");
        load();
    }

    private void load(){
        pb.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.getProfessionals(category).enqueue(new Callback<List<Professional>>() {
            @Override public void onResponse(Call<List<Professional>> call, Response<List<Professional>> res) {
                pb.setVisibility(View.GONE);
                if (res.isSuccessful() && res.body()!=null){
                    List<String> rows = new ArrayList<>();
                    for (Professional p : res.body()){
                        String line = (p.full_name!=null?p.full_name:"")
                                + (p.headline!=null? " · "+p.headline : "")
                                + (p.city!=null? " · "+p.city : "");
                        rows.add(line);
                    }
                    list.setAdapter(new ArrayAdapter<>(ProfessionalsListActivity.this,
                            android.R.layout.simple_list_item_1, rows));
                } else {
                    Toast.makeText(ProfessionalsListActivity.this, "No se pudieron cargar profesionales", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<Professional>> call, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(ProfessionalsListActivity.this, "Error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
