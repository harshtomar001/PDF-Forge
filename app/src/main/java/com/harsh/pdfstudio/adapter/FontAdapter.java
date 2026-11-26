package com.harsh.pdfstudio.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FontItem;
import java.util.List;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.FontViewHolder> {


    private OnFontClickListener listener;
    public interface OnFontClickListener {
        void onFontSelected(FontItem item);
    }

    public void setOnFontClickListener(OnFontClickListener listener) {
        this.listener = listener;
    }

    private final List<FontItem> fontList;


    private int selectedPosition = 0;

    public FontAdapter(List<FontItem> fontList,
                       OnFontClickListener listener) {

        this.fontList = fontList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FontViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_font, parent, false);

        return new FontViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull FontViewHolder holder,
            int position) {

        FontItem item = fontList.get(position);

        holder.tvFontName.setText(item.getFontName());

        holder.tvFontName.setTypeface(item.getTypeface());

        holder.imgSelected.setVisibility(
                position == selectedPosition ?
                        View.VISIBLE :
                        View.GONE
        );

        holder.itemView.setOnClickListener(v -> {

            int old = selectedPosition;

            if(selectedPosition == RecyclerView.NO_POSITION) return;

            selectedPosition = holder.getBindingAdapterPosition();


            notifyItemChanged(old);

            notifyItemChanged(selectedPosition);

            listener.onFontSelected(item);

        });

    }

    @Override
    public int getItemCount() {
        return fontList.size();
    }

    public void  setFontItem(String fontName){

        for( int i = 0; i < fontList.size(); i++ ){

            if(fontList.get(i).getFontName() == fontName){

                selectedPosition = i;
                notifyItemChanged(selectedPosition);

                break;

            }


        }

    }

    static class FontViewHolder extends RecyclerView.ViewHolder {

        TextView tvFontName;
        ImageView imgSelected;

        FontViewHolder(View itemView) {
            super(itemView);

            tvFontName = itemView.findViewById(R.id.tvFontName);
            imgSelected = itemView.findViewById(R.id.imgSelected);
        }
    }
}
