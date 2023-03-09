package com.vlite.app.helper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SimpleItemTouchCallback extends ItemTouchHelper.Callback{
    private final ItemTouchStatus itemTouchStatus;

    public SimpleItemTouchCallback(ItemTouchStatus itemTouchStatus) {
        this.itemTouchStatus = itemTouchStatus;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int swipeFlags = ItemTouchHelper.UP;
        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        this.itemTouchStatus.onItemRemove(viewHolder.getAdapterPosition());
    }
}
