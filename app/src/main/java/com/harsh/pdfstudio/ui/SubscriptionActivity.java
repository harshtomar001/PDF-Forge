package com.harsh.pdfstudio.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.databinding.SubscriptionActivityBinding;
import com.harsh.pdfstudio.repository.SubscriptionData;
import com.harsh.pdfstudio.repository.SubscriptionRepository;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class SubscriptionActivity extends AppCompatActivity {

    private SubscriptionActivityBinding binding;
    private final SubscriptionRepository subscriptionRepository = new SubscriptionRepository();

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SubscriptionActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupClickListeners();

        loadSubscriptionData();
    }

    private void setupToolbar() {

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (binding.toolbar != null) {

            ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar,
                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(WindowInsetsCompat.Type.statusBars());

                        v.setPadding(
                                v.getPaddingLeft(),
                                (int) (systemBars.top / 1.8f),
                                v.getPaddingRight(),
                                v.getPaddingBottom()
                        );

                        return insets;
                    });
        }

    }

    private void setupClickListeners() {

        binding.btnManageSubscription.setOnClickListener(v ->
                // TODO(billing): open Google Play's subscription management screen,
                // e.g. via a deep link to https://play.google.com/store/account/subscriptions
                Toast.makeText(this, "Manage Subscription", Toast.LENGTH_SHORT).show());

        binding.btnCancelSubscription.setOnClickListener(v ->
                // TODO(billing): route to Google Play cancellation flow / confirmation dialog
                Toast.makeText(this, "Cancel Subscription", Toast.LENGTH_SHORT).show());

        binding.btnUpgrade.setOnClickListener(v ->
                // TODO(billing): launch BillingClient.launchBillingFlow(...) for the premium product
                Toast.makeText(this, "Upgrade to Premium", Toast.LENGTH_SHORT).show());

        binding.rowPlan.setOnClickListener(v -> { /* TODO: show plan details */ });

        binding.rowPaymentMethod.setOnClickListener(v -> { /* TODO: show payment method */ });

        binding.rowBilling.setOnClickListener(v -> { /* TODO: open billing history */ });

    }


    private void loadSubscriptionData() {

        SubscriptionData data = subscriptionRepository.getMockSubscriptionData();
        bindUi(data);

    }

    private void bindUi(@NonNull SubscriptionData data) {

        if (data.isPremium) {
            showPremiumState(data);
        }
        else {
            showFreeState(data);
        }

    }

    private void showPremiumState(@NonNull SubscriptionData data) {

        // Plan card
        binding.tvPlanTitle.setText(getString(R.string.premium_plan_title));
        binding.tvPlanSubtitle.setText(getString(R.string.premium_plan_subtitle));

        binding.tvStatusBadge.setText(getString(R.string.status_active));
        binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_success);
        binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess));

        binding.tvUpgradeHeading.setVisibility(View.GONE);
        binding.tvPriceCaption.setVisibility(View.GONE);

        binding.tvPrice.setText(data.priceLabel);

        binding.imgCrownDecorative.setVisibility(View.VISIBLE);

        binding.layoutDates.setVisibility(View.VISIBLE);
        binding.tvStartedDate.setText(formatDate(data.purchaseDateMillis));
        binding.tvRenewsDate.setText(formatDate(data.expiryDateMillis));

        // Subscription details card
        binding.groupDetailsPremium.setVisibility(View.VISIBLE);
        binding.groupDetailsFree.setVisibility(View.GONE);
        binding.tvRowPlanValue.setText(data.planName);
        // Payment method value defaults to the "Google Play" string resource already
        // set in XML; override here in case a different provider is ever returned.
        if (data.paymentProvider != null) {
            binding.tvRowPaymentMethodValue.setText(data.paymentProvider);
        }

        // Actions
        binding.groupActionsPremium.setVisibility(View.VISIBLE);
        binding.groupActionsFree.setVisibility(View.GONE);

        // Footer
        binding.tvSecurityNote.setText(getString(R.string.security_premium));

    }

    private void showFreeState(@NonNull SubscriptionData data) {
        // Plan card

        binding.tvPlanTitle.setText(getString(R.string.free_plan_title));
        binding.tvPlanSubtitle.setText(getString(R.string.free_plan_subtitle));

        binding.tvStatusBadge.setText(getString(R.string.status_free));
        binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_neutral);
        binding.tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.colorNeutral));

        binding.tvUpgradeHeading.setVisibility(View.VISIBLE);
        binding.tvPriceCaption.setVisibility(View.VISIBLE);

        binding.tvPrice.setText(data.priceLabel);

        binding.imgCrownDecorative.setVisibility(View.GONE);
        binding.layoutDates.setVisibility(View.GONE);

        // Subscription details card
        binding.groupDetailsPremium.setVisibility(View.GONE);
        binding.groupDetailsFree.setVisibility(View.VISIBLE);

        // Actions
        binding.groupActionsPremium.setVisibility(View.GONE);
        binding.groupActionsFree.setVisibility(View.VISIBLE);

        // Footer
        binding.tvSecurityNote.setText(getString(R.string.security_free));

    }

    @NonNull
    private String formatDate(long epochMillis) {
        if (epochMillis <= 0) {
            return "-";
        }
        return DATE_FORMAT.format(epochMillis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
