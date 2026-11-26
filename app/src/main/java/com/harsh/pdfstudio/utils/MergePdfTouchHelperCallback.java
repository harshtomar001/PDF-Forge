package com.harsh.pdfstudio.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.pdfstudio.adapter.MergePdfAdapter;


public class MergePdfTouchHelperCallback
        extends ItemTouchHelper.Callback {

    private final MergePdfAdapter adapter;

    public MergePdfTouchHelperCallback(
            MergePdfAdapter adapter
    ) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {

        int dragFlags =
                ItemTouchHelper.UP |
                        ItemTouchHelper.DOWN;

        return makeMovementFlags(
                dragFlags,
                0
        );
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {

        int fromPosition =
                viewHolder.getBindingAdapterPosition();

        int toPosition =
                target.getBindingAdapterPosition();

        if (fromPosition == RecyclerView.NO_POSITION ||
                toPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        adapter.onItemMove(
                fromPosition,
                toPosition
        );

        return true;
    }

    @Override
    public void onSwiped(
            @NonNull RecyclerView.ViewHolder viewHolder,
            int direction
    ) {
        // No swipe
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }
}
