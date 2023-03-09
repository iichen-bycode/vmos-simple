package com.vlite.app.helper;

public interface ItemTouchStatus {
    boolean onItemMove(int fromPosition, int toPosition);

    boolean onItemRemove(int position);
}
