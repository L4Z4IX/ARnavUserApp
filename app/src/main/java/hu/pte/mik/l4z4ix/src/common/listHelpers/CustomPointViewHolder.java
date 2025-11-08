package hu.pte.mik.l4z4ix.src.common.listHelpers;

import android.view.View;
import android.widget.ImageButton;

import hu.pte.mik.l4z4ix.src.helloar.R;

public class CustomPointViewHolder extends CustomViewHolder {
    final ImageButton connectionButton;

    public CustomPointViewHolder(View view) {
        super(view);
        connectionButton = view.findViewById(R.id.manageConnections);
    }
}