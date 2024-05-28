package com.example.billfactu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmpresaAdapter extends RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder> {
    private List<String> empresas;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String empresa);
    }

    public EmpresaAdapter(List<String> empresas, OnItemClickListener listener) {
        this.empresas = empresas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmpresaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new EmpresaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmpresaViewHolder holder, int position) {
        String empresa = empresas.get(position);
        holder.bind(empresa, listener);
    }

    @Override
    public int getItemCount() {
        return empresas.size();
    }

    public static class EmpresaViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public EmpresaViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }

        public void bind(final String empresa, final OnItemClickListener listener) {
            textView.setText(empresa);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(empresa);
                }
            });
        }
    }
}