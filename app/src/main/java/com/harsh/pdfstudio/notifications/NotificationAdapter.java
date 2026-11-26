package com.harsh.pdfstudio.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the notification list. All type-specific styling (icon,
 * icon background, unread appearance) lives here so NotificationActivity
 * stays focused on state/filtering, not view binding.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    /** Callback for a full-card click. */
    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item, int position);
    }

    private final List<NotificationItem> items = new ArrayList<>();
    private final OnNotificationClickListener clickListener;

    public NotificationAdapter(OnNotificationClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /** Replaces the current dataset and refreshes the list. */
    public void submitList(List<NotificationItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<NotificationItem> getCurrentList() {
        return items;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        holder.bind(item, clickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardRoot;
        private final View iconBackground;
        private final ImageView iconImage;
        private final TextView title;
        private final TextView description;
        private final TextView time;
        private final View unreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = (MaterialCardView) itemView;
            iconBackground = itemView.findViewById(R.id.icon_background);
            iconImage = itemView.findViewById(R.id.icon_image);
            title = itemView.findViewById(R.id.tv_title);
            description = itemView.findViewById(R.id.tv_description);
            time = itemView.findViewById(R.id.tv_time);
            unreadDot = itemView.findViewById(R.id.unread_dot);
        }

        void bind(NotificationItem item, OnNotificationClickListener listener) {

            title.setText(item.getTitle());
            description.setText(item.getDescription());
            time.setText(item.getDisplayTime());

            switch (item.getType()) {

                case SUCCESS:

                    iconBackground.setBackgroundResource(R.drawable.bg_icon_circle_success);
                    iconImage.setImageResource(R.drawable.ic_check_circle);
                    iconImage.setContentDescription(
                            itemView.getContext().getString(R.string.content_desc_success_icon));
                    break;

                case INFO:

                    iconBackground.setBackgroundResource(R.drawable.bg_icon_circle_info);
                    iconImage.setImageResource(R.drawable.ic_info);
                    iconImage.setContentDescription(
                            itemView.getContext().getString(R.string.content_desc_info_icon));
                    break;

                case WARNING:

                    iconBackground.setBackgroundResource(R.drawable.bg_icon_circle_warning);
                    iconImage.setImageResource(R.drawable.ic_warning);
                    iconImage.setContentDescription(
                            itemView.getContext().getString(R.string.content_desc_warning_icon));
                    break;

            }

            // Unread vs read visual treatment.
            if (item.isRead()) {

                cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.card_background));
                title.setTypeface(title.getTypeface(), android.graphics.Typeface.NORMAL);
                unreadDot.setVisibility(View.INVISIBLE);

            }
            else {

                cardRoot.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.blue_checkBox_unread));
                title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
                unreadDot.setVisibility(View.VISIBLE);

            }

            itemView.setOnClickListener(v -> {

                if (listener != null) {
                    listener.onNotificationClick(item, getBindingAdapterPosition());
                }

            });
        }
    }
}
