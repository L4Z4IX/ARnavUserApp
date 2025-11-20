package hu.pte.mik.l4z4ix.src.Components.listHelpers;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hu.pte.mik.l4z4ix.src.ArApplication.R;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Connection;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Point;

public class CustomConnectionAdapter extends RecyclerView.Adapter<CustomConnectionViewHolder> {
    final List<Point> points = new ArrayList<>();
    final List<Point> connectedTo;
    final ConnectionFormHandler handler;
    final Point currentPoint;


    public CustomConnectionAdapter(List<Point> points, List<Connection> connections, ConnectionFormHandler handler, Point currentPoint) {
        this.points.addAll(points);
        this.handler = handler;
        this.currentPoint = currentPoint;
        this.points.remove(currentPoint);
        connectedTo = findConnectedPoints(currentPoint, points, connections);
    }

    @Override
    public CustomConnectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_connectionlist_item, parent, false);
        return new CustomConnectionViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(CustomConnectionViewHolder holder, int position) {
        Point item = points.get(position);
        holder.textView.setText(item.getName());
        holder.mySwitch.setChecked(connectedTo.contains(item));
        holder.mySwitch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                handler.onSwitchChanged(item, !holder.mySwitch.isChecked());
                v.performClick();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    private List<Point> findConnectedPoints(Point selectedPoint, List<Point> allPoints, List<Connection> connections) {
        Set<Long> connectedIds = new HashSet<>();
        for (Connection conn : connections) {
            if (conn.getPoint1Id().equals(selectedPoint.getId())) {
                connectedIds.add(conn.getPoint2Id());
            } else if (conn.getPoint2Id().equals(selectedPoint.getId())) {
                connectedIds.add(conn.getPoint1Id());
            }
        }

        return allPoints.stream()
                .filter(p -> connectedIds.contains(p.getId()))
                .collect(Collectors.toList());
    }
}
