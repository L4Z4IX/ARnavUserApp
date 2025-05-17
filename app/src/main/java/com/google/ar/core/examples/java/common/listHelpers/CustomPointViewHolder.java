package com.google.ar.core.examples.java.common.listHelpers;

import android.view.View;
import android.widget.ImageButton;

import com.google.ar.core.examples.java.helloar.R;

public class CustomPointViewHolder extends CustomViewHolder {
    final ImageButton connectionButton;

    public CustomPointViewHolder(View view) {
        super(view);
        connectionButton = view.findViewById(R.id.manageConnections);
    }
}