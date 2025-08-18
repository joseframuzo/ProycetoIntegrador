package com.pintegrador.servicios.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.pintegrador.servicios.R;
import com.pintegrador.servicios.network.dto.ProfessionalPhoto;
import java.util.ArrayList;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.VH> {
    private final List<ProfessionalPhoto> data = new ArrayList<>();
    public void setData(List<ProfessionalPhoto> list){ data.clear(); if (list!=null) data.addAll(list); notifyDataSetChanged(); }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){ return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.row_photo,p,false)); }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){ Glide.with(h.iv).load(data.get(pos).url).placeholder(R.drawable.ic_architect).into(h.iv); }
    @Override public int getItemCount(){ return data.size(); }
    static class VH extends RecyclerView.ViewHolder{ ImageView iv; VH(View v){ super(v); iv=v.findViewById(R.id.iv);} }
}
