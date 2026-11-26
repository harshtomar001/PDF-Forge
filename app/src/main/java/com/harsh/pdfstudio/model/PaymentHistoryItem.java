package com.harsh.pdfstudio.model;

public class PaymentHistoryItem {

    private final String date;
    private final String title;
    private final String orderId;
    private final String amount;
    private final PaymentStatus status;

    public enum PaymentStatus {
        PAID,
        FAILED,
        REFUNDED
    }

    public PaymentHistoryItem(String date, String title, String orderId,
                               String amount, PaymentStatus status) {
        this.date = date;
        this.title = title;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
