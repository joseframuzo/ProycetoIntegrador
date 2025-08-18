package com.pintegrador.servicios.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pintegrador.servicios.R;
import com.pintegrador.servicios.network.dto.Review;
import java.util.ArrayList;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.VH> {
    private final List<Review> data = new ArrayList<>();
    public void setData(List<Review> list){ data.clear(); if (list!=null) data.addAll(list); notifyDataSetChanged(); }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){ return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.row_review,p,false)); }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Review r = data.get(pos);
        h.tvUser.setText(r.user_name);
        h.rb.setRating(r.rating);
        h.tvComment.setText(r.comment);
    }
    @Override public int getItemCount(){ return data.size(); }
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvUser, tvComment; final RatingBar rb;
        VH(View v){ super(v); tvUser=v.findViewById(R.id.tvUser); tvComment=v.findViewById(R.id.tvComment); rb=v.findViewById(R.id.rb); }
    }
}
