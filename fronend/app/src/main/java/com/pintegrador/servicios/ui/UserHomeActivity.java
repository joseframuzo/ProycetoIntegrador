package com.pintegrador.servicios.ui;

import android.content.Intent;
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
import com.pintegrador.servicios.network.dto.Category;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserHomeActivity extends AppCompatActivity {
    private ListView list;
    private ProgressBar pb;
    private List<Category> cats = new ArrayList<>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        list = findViewById(R.id.listCategories);
        pb = findViewById(R.id.pb);
        loadCategories();
    }

    private void loadCategories(){
        pb.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.get(this).create(ApiService.class);
        api.getCategories().enqueue(new Callback<List<Category>>() {
            @Override public void onResponse(Call<List<Category>> call, Response<List<Category>> res) {
                pb.setVisibility(View.GONE);
                if (res.isSuccessful() && res.body()!=null){
                    cats = res.body();
                    List<String> names = new ArrayList<>();
                    for (Category c : cats) names.add(c.name);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            UserHomeActivity.this,
                            android.R.layout.simple_list_item_1,
                            names
                    );
                    list.setAdapter(adapter);

                    list.setOnItemClickListener((a, v, pos, id) -> {
                        Category c = cats.get(pos); // usamos el slug real
                        Intent i = new Intent(UserHomeActivity.this, ProfessionalsListActivity.class);
                        i.putExtra("category", c.slug);
                        startActivity(i);
                    });
                } else {
                    Toast.makeText(UserHomeActivity.this, "No se pudieron cargar categorías", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(UserHomeActivity.this, "Error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
