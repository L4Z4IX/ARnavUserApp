package hu.pte.mik.l4z4ix.src.Components.listHelpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CustomAdapter<T, VH extends CustomViewHolder> extends RecyclerView.Adapter<VH> {
    final List<T> items;
    final FormHandler<T> handler;
    final OnItemClickListener<T> onItemClickListener;
    final Class<VH> viewHolderClazz;
    final private int resource;


    public CustomAdapter(int resource, List<T> items, FormHandler<T> handler, OnItemClickListener<T> onItemClickListener, Class<VH> viewHolderClazz) {
        this.items = items;
        this.handler = handler;
        this.onItemClickListener = onItemClickListener;
        this.viewHolderClazz = viewHolderClazz;
        this.resource = resource;
    }


    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);
        try {
            return viewHolderClazz.getDeclaredConstructor(View.class).newInstance(view);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onBindViewHolder(VH holder, int position) {
        T item = items.get(position);
        holder.textView.setText(item.toString()); // Customize if needed
        holder.editButton.setOnClickListener(v -> handler.onEditButtonClick(item));
        holder.removeButton.setOnClickListener(v -> handler.onRemoveButtonClick(item));
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}