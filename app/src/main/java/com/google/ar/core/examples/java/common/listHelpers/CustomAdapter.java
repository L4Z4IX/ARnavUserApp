package com.google.ar.core.examples.java.common.listHelpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.ar.core.examples.java.helloar.R;

import java.util.List;

public class CustomAdapter<T> extends RecyclerView.Adapter<CustomViewHolder> {
    private final List<T> items;
    private final FormHandler<T> handler;


    public CustomAdapter(List<T> items, FormHandler<T> handler) {
        this.items = items;
        this.handler = handler;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_list_item, parent, false);
        return new CustomViewHolder(view);
    }


    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        T item = items.get(position);
        holder.textView.setText(item.toString()); // Customize if needed
        holder.editButton.setOnClickListener(v -> handler.onEditButtonClick(item));
        holder.removeButton.setOnClickListener(v -> handler.onRemoveButtonClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}