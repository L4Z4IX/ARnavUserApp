package hu.pte.mik.l4z4ix.src.Components.listHelpers;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import hu.pte.mik.l4z4ix.src.ArApplication.R;

public class CustomConnectionViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;
    final Switch mySwitch;

    public CustomConnectionViewHolder(View view) {
        super(view);
        textView = view.findViewById(R.id.list_item_name);
        mySwitch = view.findViewById(R.id.isConnected);
    }
}