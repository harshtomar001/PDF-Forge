package com.harsh.pdfstudio.repository;


public class SubscriptionData {

    /** Whether the user currently holds an active premium subscription. */
    public final boolean isPremium;

    /** Human-readable plan name, e.g. "Premium" or "Free". */
    public final String planName;

    /** Google Play subscription/product id, e.g. "pdfstudio_premium_monthly". Null for free users. */
    public final String subscriptionId;

    /** Opaque purchase token returned by Google Play Billing. Null for free users. */
    public final String purchaseToken;

    /** Display price string, e.g. "₹299 / month". */
    public final String priceLabel;

    /** Purchase/start date in epoch millis. 0 if not applicable. */
    public final long purchaseDateMillis;

    /** Next renewal / expiry date in epoch millis. 0 if not applicable. */
    public final long expiryDateMillis;

    /** Whether Google Play will auto-renew this subscription. */
    public final boolean autoRenewing;

    /** Payment provider label, e.g. "Google Play". */
    public final String paymentProvider;

    public SubscriptionData(boolean isPremium,
                             String planName,
                             String subscriptionId,
                             String purchaseToken,
                             String priceLabel,
                             long purchaseDateMillis,
                             long expiryDateMillis,
                             boolean autoRenewing,
                             String paymentProvider) {
        this.isPremium = isPremium;
        this.planName = planName;
        this.subscriptionId = subscriptionId;
        this.purchaseToken = purchaseToken;
        this.priceLabel = priceLabel;
        this.purchaseDateMillis = purchaseDateMillis;
        this.expiryDateMillis = expiryDateMillis;
        this.autoRenewing = autoRenewing;
        this.paymentProvider = paymentProvider;
    }

}
