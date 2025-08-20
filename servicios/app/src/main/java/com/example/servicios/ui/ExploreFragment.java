package com.example.servicios.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.servicios.R;
import com.example.servicios.api.ApiClient;
import com.example.servicios.api.ApiService;
import com.example.servicios.models.ProfessionalCard;

import java.util.*;
import retrofit2.*;

public class ExploreFragment extends Fragment implements ProfessionalsAdapter.OnItemClick {

    private static final String TAG = "ExploreFragment";

    private RecyclerView rv;
    private ProfessionalsAdapter adapter;
    private ApiService api;

    private TextInputEditText edtQuery;
    private MaterialAutoCompleteTextView edtProfession, edtCity;

    private MaterialButton btnSearch;
    private TextView emptyView;

    // mapa Nombre visible -> slug de BD
    private static final Map<String, String> PROF_MAP = new HashMap<String, String>() {{
        put("Arquitecto", "arquitecto");
        put("Plomero",    "plomero");
        put("Pintor",     "pintor");
        put("Albañil",    "albanil"); // sin ñ en slug
    }};

    // Estado de filtros actuales
    private String currentQ = null;
    private String currentCategory = null; // slug
    private String currentCity = null;

    // Datos y paginación
    private final List<ProfessionalCard> data = new ArrayList<>();
    private int limit = 20;
    private int offset = 0;
    private boolean noMore = false;

    private EndlessScrollListener scrollListener;

    private final Handler debounce = new Handler(Looper.getMainLooper());
    private Runnable debouncedSearch;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_explore, c, false);

        rv = v.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfessionalsAdapter(data, this);
        rv.setAdapter(adapter);

        edtQuery = v.findViewById(R.id.edtQuery);
        edtProfession = v.findViewById(R.id.edtProfession);
        edtCity = v.findViewById(R.id.edtCity);
        btnSearch = v.findViewById(R.id.btnSearch);
        emptyView = v.findViewById(R.id.emptyView);


        api = ApiClient.get().create(ApiService.class);

        // Paginación
        scrollListener = new EndlessScrollListener() {
            @Override public void onLoadMore() {
                if (!noMore) fetchMore();
            }
        };
        rv.addOnScrollListener(scrollListener);

        // Dropdowns (visuales)
        setDropdowns();

        // Listeners
        btnSearch.setOnClickListener(view -> applyServerFilters());

        // (Opcional) búsqueda al teclear con debounce
        edtQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                if (debouncedSearch!=null) debounce.removeCallbacks(debouncedSearch);
                debouncedSearch = () -> applyServerFilters();
                debounce.postDelayed(debouncedSearch, 350);
            }
            @Override public void afterTextChanged(Editable s){}
        });



        applyServerFilters();

        return v;
    }

    private void setDropdowns() {
        // Nombres bonitos para UI
        List<String> profs = Arrays.asList("Todas", "Arquitecto", "Plomero", "Pintor", "Albañil");
        edtProfession.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, profs));

        List<String> cities = Arrays.asList("Todas", "Quito", "Guayaquil", "Cuenca", "Manta");
        edtCity.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, cities));
    }

    /** Aplica filtros: reinicia paginación y trae del servidor. */
    private void applyServerFilters() {
        currentQ = textOrNull(edtQuery);
        currentCategory = normalizedCategory(edtProfession); // Nombre → slug → null si "Todas"
        currentCity = normalizedCity(edtCity);               // null si "Todas"

        // Log para ver qué se envía
        Log.d(TAG, "applyServerFilters() q=" + currentQ + " category=" + currentCategory + " city=" + currentCity);

        // reset paginación
        data.clear();
        adapter.notifyDataSetChanged();
        offset = 0;
        noMore = false;
        if (scrollListener != null) scrollListener.reset();

        fetchMore();
    }

    /** Trae siguiente página desde el servidor con filtros actuales. */
    private void fetchMore() {
        api.listProfessionals(
                currentCity,
                currentCategory,
                currentQ,
                limit,
                offset
        ).enqueue(new Callback<List<ProfessionalCard>>() {
            @Override public void onResponse(Call<List<ProfessionalCard>> call, Response<List<ProfessionalCard>> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body()!=null) {
                    List<ProfessionalCard> page = r.body();

                    Log.d(TAG, "fetchMore() got " + page.size() + " items; offset=" + offset);

                    int start = data.size();
                    data.addAll(page);
                    adapter.notifyItemRangeInserted(start, page.size());

                    if (page.size() < limit) noMore = true;
                    offset += page.size();

                    emptyView.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(getContext(), "Error listando", Toast.LENGTH_SHORT).show();
                    emptyView.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override public void onFailure(Call<List<ProfessionalCard>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                emptyView.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private String textOrNull(TextInputEditText t){
        String s = (t.getText()==null) ? "" : t.getText().toString().trim();
        return s.isEmpty()? null : s;
    }

    private String normalizedCategory(MaterialAutoCompleteTextView t){
        String s = (t.getText()==null) ? "" : t.getText().toString().trim();
        if (s.isEmpty() || s.equalsIgnoreCase("todas")) return null;
        // Mapea nombre visible -> slug
        String slug = PROF_MAP.get(s);
        return slug; // puede ser null si teclearon algo no listado
    }

    private String normalizedCity(MaterialAutoCompleteTextView t){
        String s = (t.getText()==null) ? "" : t.getText().toString().trim();
        if (s.isEmpty() || s.equalsIgnoreCase("todas")) return null;
        return s; // tu backend acepta city libre (probablemente ILIKE)
    }

    @Override public void onClick(ProfessionalCard card) {
        android.content.Intent i = new android.content.Intent(getContext(), DetailActivity.class);
        i.putExtra("PROF_ID", card.professional_id);
        startActivity(i);
    }
}
