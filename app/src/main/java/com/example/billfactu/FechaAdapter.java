package com.example.billfactu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FechaAdapter extends RecyclerView.Adapter<FechaAdapter.FechaViewHolder> {
    private List<String> fechas;
    private OnItemClickListener listener;

    public FechaAdapter(List<String> fechas, OnItemClickListener listener) {
        this.fechas = fechas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FechaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new FechaViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FechaViewHolder holder, int position) {
        String fecha = fechas.get(position);
        holder.bind(fecha);
    }

    @Override
    public int getItemCount() {
        return fechas.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String fecha);
    }

    static class FechaViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        OnItemClickListener listener;

        public FechaViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
            this.listener = listener;
        }

        public void bind(String fecha) {
            textView.setText(fecha);
            itemView.setOnClickListener(v -> listener.onItemClick(fecha));
        }
    }
}