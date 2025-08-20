package com.example.servicios.ui;


import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.servicios.R;
import com.example.servicios.models.ProfessionalCard;
import java.util.List;

public class ProfessionalsAdapter extends RecyclerView.Adapter<ProfessionalsAdapter.VH> {

    public interface OnItemClick { void onClick(ProfessionalCard card); }

    private List<ProfessionalCard> data;
    private final OnItemClick onItemClick;

    public ProfessionalsAdapter(List<ProfessionalCard> data, OnItemClick cb) {
        this.data = data; this.onItemClick = cb;
    }

    public void setData(List<ProfessionalCard> d) { this.data = d; notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_professional, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ProfessionalCard c = data.get(pos);
        h.txtName.setText(c.full_name);
        h.txtHeadline.setText(c.headline != null ? c.headline : c.main_category);
        h.txtCity.setText(c.city != null ? c.city : "");
        h.itemView.setOnClickListener(v -> onItemClick.onClick(c));
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtHeadline, txtCity;
        VH(View v) { super(v);
            txtName = v.findViewById(R.id.txtName);
            txtHeadline = v.findViewById(R.id.txtHeadline);
            txtCity = v.findViewById(R.id.txtCity);
        }
    }
}
