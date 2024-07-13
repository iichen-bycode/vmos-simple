package com.gmspace.app.helper;

public interface ItemTouchStatus {
    boolean onItemMove(int fromPosition, int toPosition);

    boolean onItemRemove(int position);
}
