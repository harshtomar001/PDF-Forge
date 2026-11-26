package com.harsh.pdfstudio.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import java.util.List;

/**
 * Adapter for the horizontal "Recent Colors" list in the color picker.
 * Highlights whichever swatch matches the currently active color.
 */

public class RecentColorsAdapter extends RecyclerView.Adapter<RecentColorsAdapter.ColorViewHolder> {

    public interface OnColorClickListener {
        void onColorClick(int color, int position);
    }

    private final List<Integer> colorList;
    private final OnColorClickListener listener;

    private int selectedColor = Color.TRANSPARENT;

    public RecentColorsAdapter(List<Integer> colorList, OnColorClickListener listener) {
        this.colorList = colorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_colors, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        int color = colorList.get(position);
        holder.bind(color, color == selectedColor, listener, position);
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    /**
     * Call this when the user picks a color elsewhere in the picker
     * (e.g. from the saturation/hue sliders) so the matching recent
     * swatch gets highlighted, if present.
     */
    public void setSelectedColor(int color) {
        int previousSelected = colorList.indexOf(selectedColor);
        selectedColor = color;
        int newSelected = colorList.indexOf(selectedColor);

        if (previousSelected != -1) notifyItemChanged(previousSelected);
        if (newSelected != -1) notifyItemChanged(newSelected);
    }

    /**
     * Adds a new color to the front of the list, avoiding duplicates,
     * and trims the list down to maxSize (default suggestion: 8).
     */
    public void addColor(int color, int maxSize) {
        int existingIndex = colorList.indexOf(color);
        if (existingIndex == 0) return; // already at front

        if (existingIndex > 0) {
            colorList.remove(existingIndex);
            notifyItemRemoved(existingIndex);
        }

        colorList.add(0, color);
        notifyItemInserted(0);

        while (colorList.size() > maxSize) {
            int lastIndex = colorList.size() - 1;
            colorList.remove(lastIndex);
            notifyItemRemoved(lastIndex);
        }
    }

    public void clearColors() {
        int size = colorList.size();
        colorList.clear();
        notifyItemRangeRemoved(0, size);
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardRecentColor;

        ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRecentColor = itemView.findViewById(R.id.cardRecentColor);
        }

        void bind(int color, boolean isSelected, OnColorClickListener listener, int position) {

            cardRecentColor.setCardBackgroundColor(color);

            cardRecentColor.setStrokeWidth(isSelected ? dp(2, itemView) : 0);

            cardRecentColor.setContentDescription(String.format("#%06X", 0xFFFFFF & color));

            cardRecentColor.setOnClickListener(v -> {

                if (listener != null) listener.onColorClick(color, position);

            });
        }

        private int dp(int value, View view) {

            float density = view.getResources().getDisplayMetrics().density;

            return Math.round(value * density);
        }
    }
}