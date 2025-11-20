package hu.pte.mik.l4z4ix.src.Components.listHelpers;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import hu.pte.mik.l4z4ix.src.ArApplication.R;

public class CustomViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;
    final ImageButton editButton, removeButton;

    public CustomViewHolder(View view) {
        super(view);
        textView = view.findViewById(R.id.list_item_name);
        editButton = view.findViewById(R.id.editButton);
        removeButton = view.findViewById(R.id.removeButton);
    }
}