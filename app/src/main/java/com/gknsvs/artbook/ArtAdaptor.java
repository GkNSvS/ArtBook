package com.gknsvs.artbook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gknsvs.artbook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdaptor extends RecyclerView.Adapter<ArtAdaptor.ArtHolder> {
    @NonNull
    ArrayList<Art> artArrayList;

    public ArtAdaptor(@NonNull ArrayList<Art> artArrayList) {
        this.artArrayList = artArrayList;
    }

    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        int myPosition=position;
        holder.binding.recyclerViewTextView.setText(artArrayList.get(myPosition).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("intentInfo","chosen");
                intent.putExtra("id",artArrayList.get(myPosition).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder{
        RecyclerRowBinding binding;
        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
