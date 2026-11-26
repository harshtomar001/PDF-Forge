package com.harsh.pdfstudio.utils;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.pdfstudio.adapter.EditPDF_adap;

public class Drag_n_Drop  {
    EditPDF_adap previewAdapter;

    public Drag_n_Drop(EditPDF_adap  adapter){
        this.previewAdapter=adapter;

    }
    public ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP |
                                ItemTouchHelper.DOWN |
                                ItemTouchHelper.LEFT |
                                ItemTouchHelper.RIGHT,
                                            0) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {

                        int fromPos = viewHolder.getAdapterPosition();
                        int toPos = target.getAdapterPosition();

                        previewAdapter.itemsExchange(fromPos,toPos);

                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                    }
                    @Override
                    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                        int position = viewHolder.getAdapterPosition();





                        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

                        return makeMovementFlags(dragFlags, 0);
                    }

                };


}
