package com.harsh.pdfstudio.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.harsh.pdfstudio.R;


public class ImgAdapter extends ListAdapter<Uri, ImgAdapter.ViewHolder> {

    private final onClickListeners listener;

    private static final DiffUtil.ItemCallback<Uri> DIFF_CALLBACK = new DiffUtil.ItemCallback<Uri>() {
        @Override
        public boolean areItemsTheSame(@NonNull Uri a, @NonNull Uri b) {
            return a.equals(b);
        }
        @Override
        public boolean areContentsTheSame(@NonNull Uri a, @NonNull Uri b) {
            return a.equals(b);
        }
    };

    public ImgAdapter(onClickListeners listener) {

        super(DIFF_CALLBACK);
        this.listener = listener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.create_pdf_item_img_layout,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(holder.itemView.getContext())
                .load(getItem(position))
                .thumbnail(0.4f)
                .into(holder.image);

        holder.pageNum.setText(String.valueOf(position + 1));

        holder.removeImage.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition(); // live lookup, not captured
            if (currentPos == RecyclerView.NO_POSITION) return;  // view was removed/recycled mid-tap

            Uri uri = getItem(currentPos); // fetch the item that's ACTUALLY there right now
            listener.onRemove(uri);
        });

        holder.image.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            Uri uri = getItem(currentPos);
            listener.onClick(uri);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView removeImage;
        TextView pageNum;

        ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.imageContainer);
            removeImage = view.findViewById(R.id.remove_image);
            pageNum = view.findViewById(R.id.image_number);
        }
    }

    public interface onClickListeners {
        void onRemove(Uri uri);
        void onClick(Uri uri);
    }

}