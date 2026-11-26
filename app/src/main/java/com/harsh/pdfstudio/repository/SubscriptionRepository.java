package com.harsh.pdfstudio.repository;

import java.util.Calendar;



public class SubscriptionRepository {

    /**
     * Flip this flag to test both UI states until real Billing data is wired up.
     */
    private static final boolean MOCK_IS_PREMIUM = true;

    public SubscriptionData getMockSubscriptionData() {
        if (MOCK_IS_PREMIUM) {
            Calendar started = Calendar.getInstance();
            started.set(2026, Calendar.AUGUST, 30);

            Calendar renews = Calendar.getInstance();
            renews.set(2026, Calendar.SEPTEMBER, 30);

            return new SubscriptionData(
                    /* isPremium */ true,
                    /* planName */ "Premium",
                    /* subscriptionId */ "pdfstudio_premium_monthly",
                    /* purchaseToken */ "mock-purchase-token-abc123",
                    /* priceLabel */ "\u20B9299 / month",
                    /* purchaseDateMillis */ started.getTimeInMillis(),
                    /* expiryDateMillis */ renews.getTimeInMillis(),
                    /* autoRenewing */ true,
                    /* paymentProvider */ "Google Play"
            );
        }

        return new SubscriptionData(
                /* isPremium */ false,
                /* planName */ "Free",
                /* subscriptionId */ null,
                /* purchaseToken */ null,
                /* priceLabel */ "\u20B9299 / month",
                /* purchaseDateMillis */ 0L,
                /* expiryDateMillis */ 0L,
                /* autoRenewing */ false,
                /* paymentProvider */ null
        );
    }
}
