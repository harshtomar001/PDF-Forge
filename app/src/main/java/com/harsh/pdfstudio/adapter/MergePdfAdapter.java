package com.harsh.pdfstudio.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.databinding.ItemMergePdfBinding;
import com.harsh.pdfstudio.model.MergePdfItem;


public class MergePdfAdapter
        extends ListAdapter<MergePdfItem, MergePdfAdapter.PdfViewHolder> {

    public interface Listener {

        void onItemDeleted(MergePdfItem item);

        void onItemMoved(
                int fromPosition,
                int toPosition
        );

        void onDragHandleTouched(
                PdfViewHolder holder
        );
    }

    private final Listener listener;

    private static final DiffUtil.ItemCallback<MergePdfItem>
            DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MergePdfItem>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull MergePdfItem oldItem,
                        @NonNull MergePdfItem newItem
                ) {
                    return oldItem.getId()
                            .equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull MergePdfItem oldItem,
                        @NonNull MergePdfItem newItem
                ) {
                    return oldItem.equals(newItem);
                }
            };


    public MergePdfAdapter(Listener listener) {

        super(DIFF_CALLBACK);

        this.listener = listener;
    }


    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        ItemMergePdfBinding binding =
                ItemMergePdfBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new PdfViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(
            @NonNull PdfViewHolder holder,
            int position
    ) {

        MergePdfItem item = getItem(position);

        holder.binding.tvFileName.setText(
                item.getName()
        );

        holder.binding.tvFileSize.setText(
                item.getFormattedSize()
        );


        holder.binding.ivDelete.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            MergePdfItem deleteItem =
                    getItem(adapterPosition);

            if (listener != null) {

                listener.onItemDeleted(
                        deleteItem
                );
            }
        });


        holder.binding.ivDragHandle.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {

                        if (listener != null) {

                            listener.onDragHandleTouched(
                                    holder
                            );
                        }
                    }

                    return true;
                }
        );
    }


    /**
     * Called by ItemTouchHelper when
     * an item is moved.
     *
     * This just forwards to the listener, which calls
     * MergePdfViewModel.swapPDFS() -> pdfs.setValue(updated). Because
     * setValue() dispatches to observers synchronously when called on the
     * main thread, MergePdfInputFragment's observer re-invokes
     * adapter.submitList(files) within this same call stack — so the
     * adapter still gets its "immediate" update per the ItemTouchHelper
     * contract, without this method also submitting locally first (which
     * would diff the same swap twice for no benefit).
     */
    public void onItemMove(
            int fromPosition,
            int toPosition
    ) {

        if (listener != null) {

            listener.onItemMoved(
                    fromPosition,
                    toPosition
            );
        }
    }


    static public class PdfViewHolder
            extends RecyclerView.ViewHolder {

        final ItemMergePdfBinding binding;

        PdfViewHolder(
                ItemMergePdfBinding binding
        ) {

            super(binding.getRoot());

            this.binding = binding;
        }
    }
}