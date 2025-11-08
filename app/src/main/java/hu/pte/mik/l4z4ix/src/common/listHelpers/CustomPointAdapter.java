package hu.pte.mik.l4z4ix.src.common.listHelpers;

import java.util.List;

import hu.pte.mik.l4z4ix.src.common.entityModel.Point;

public class CustomPointAdapter extends CustomAdapter<Point, CustomPointViewHolder> {


    public CustomPointAdapter(int resource, List<Point> items, PointFormHandler handler, OnItemClickListener<Point> onItemClickListener) {
        super(resource, items, handler, onItemClickListener, CustomPointViewHolder.class);
    }


    @Override
    public void onBindViewHolder(CustomPointViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Point item = items.get(position);
        holder.connectionButton.setOnClickListener(v -> ((PointFormHandler) (handler)).onManageConnectionClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}