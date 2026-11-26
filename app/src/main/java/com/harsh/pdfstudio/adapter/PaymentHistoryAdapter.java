package com.harsh.pdfstudio.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.PaymentHistoryItem;

import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.RowViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(PaymentHistoryItem item);
    }

    private final List<PaymentHistoryItem> items;
    private final OnTransactionClickListener listener;

    public PaymentHistoryAdapter(List<PaymentHistoryItem> items,
                                 OnTransactionClickListener listener) {

        this.items = items;
        this.listener = listener;

    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_history, parent, false);
        return new RowViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {

        PaymentHistoryItem item = items.get(position);

        holder.date.setText(item.getDate());
        holder.title.setText(item.getTitle());
        holder.orderId.setText(holder.orderId.getContext()
                .getString(R.string.row_order_id_format, item.getOrderId()));
        holder.amount.setText(item.getAmount());

        switch (item.getStatus()) {

            case PAID:

                holder.status.setText(R.string.badge_paid);
                holder.status.setBackgroundResource(
                        R.drawable.bg_pill_success_container
                );
                holder.status.setTextColor(
                        ContextCompat.getColor(
                                holder.status.getContext(),
                                R.color.color_success
                        )
                );

                break;

            case FAILED:

                holder.status.setText(R.string.badge_failed);
                holder.status.setBackgroundResource(
                        R.drawable.bg_pill_error_container
                );
                holder.status.setTextColor(
                        ContextCompat.getColor(
                                holder.status.getContext(),
                                R.color.color_error
                        )
                );

                break;

            case REFUNDED:

                holder.status.setText(R.string.badge_refunded);
                holder.status.setBackgroundResource(
                        R.drawable.bg_pill_primary_container
                );
                holder.status.setTextColor(
                        ContextCompat.getColor(
                                holder.status.getContext(),
                                R.color.color_on_primary_container
                        )
                );

                break;
        }

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onTransactionClick(item);
            }

        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        final TextView date;
        final TextView title;
        final TextView orderId;
        final TextView amount;
        final TextView status;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.txtRowDate);
            title = itemView.findViewById(R.id.txtRowTitle);
            orderId = itemView.findViewById(R.id.txtRowOrderId);
            amount = itemView.findViewById(R.id.txtRowAmount);
            status = itemView.findViewById(R.id.txtRowStatus);
        }

    }
}
