package com.google.ar.core.examples.java.common.listHelpers;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.ar.core.examples.java.helloar.R;

public class CustomConnectionViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;
    final Switch mySwitch;

    public CustomConnectionViewHolder(View view) {
        super(view);
        textView = view.findViewById(R.id.list_item_name);
        mySwitch = view.findViewById(R.id.isConnected);
    }
}