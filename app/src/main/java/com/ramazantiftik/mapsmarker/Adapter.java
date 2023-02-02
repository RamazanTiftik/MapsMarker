package com.ramazantiftik.mapsmarker;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramazantiftik.mapsmarker.databinding.RecyclerRowBinding;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.PlaceHolder> {

    List<Model> modelList;

    public Adapter(List<Model> modelList){
        this.modelList=modelList;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PlaceHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        holder.binding.recyclerRow.setText(modelList.get(position).placeName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.itemView.getContext(),MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",modelList.get(holder.getAdapterPosition()));
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder{

        RecyclerRowBinding binding;
        public PlaceHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }

}
