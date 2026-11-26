package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.harsh.pdfstudio.R;

public class UpgradeActivity extends FontBaseActivity {

    private static final String TERMS_OF_SERVICE_URL = "https://pdfstudioapp.github.io/pdfstudio.github.io/terms_services.html";

    private enum Plan {
        MONTHLY,
        YEARLY
    }

    private Plan selectedPlan = Plan.YEARLY;

    private MaterialToolbar toolbar;
    private MaterialCardView monthlyPlanCard;
    private MaterialCardView yearlyPlanCard;
    private MaterialRadioButton monthlyRadio;
    private MaterialRadioButton yearlyRadio;
    private TextView monthlyPrice;
    private TextView yearlyPrice;
    private TextView heroPrice;
    private TextView heroPeriod;
    private MaterialButton upgradeNowButton;
    private TextView restorePurchaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        bindViews();
        setupWindowInsets();
        setupToolbar();
        setupHeroBranding();
        setupBenefitItems();
        setupPlanSelection();
        setupUpgradeButton();
        setupRestorePurchase();
        setupLegalLinks();

        // Yearly is the default selection, matching the supplied design.
        updatePlanUI();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.upgradeToolbar);

        monthlyPlanCard = findViewById(R.id.monthlyPlanCard);
        yearlyPlanCard = findViewById(R.id.yearlyPlanCard);

        monthlyRadio = findViewById(R.id.monthlyRadio);
        yearlyRadio = findViewById(R.id.yearlyRadio);

        monthlyPrice = findViewById(R.id.monthlyPrice);
        yearlyPrice = findViewById(R.id.yearlyPrice);

        heroPrice = findViewById(R.id.heroPrice);
        heroPeriod = findViewById(R.id.heroPeriod);

        upgradeNowButton = findViewById(R.id.upgradeNowButton);
        restorePurchaseButton = findViewById(R.id.restorePurchaseButton);
    }

    private void setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View root = findViewById(R.id.upgradeRoot);
        View scroll = findViewById(R.id.upgradeScroll);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.toolbar_blue));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.upgrade_background));

        ViewCompat.getWindowInsetsController(root);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());


            // The toolbar is drawn behind the status bar, so add the top inset
            // to its content while preserving the 56dp toolbar height.
            toolbar.setPadding(
                    toolbar.getPaddingLeft(),
                    (int) (systemBars.top / 2f),
                    toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom()
            );

            // Keep the final legal controls above the navigation/gesture area.
            scroll.setPadding(
                    scroll.getPaddingLeft(),
                    scroll.getPaddingTop(),
                    scroll.getPaddingRight(),
                    systemBars.bottom + dpToPx(28)
            );

            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupHeroBranding() {
        TextView heroBrandTitle = findViewById(R.id.heroBrandTitle);

        SpannableString brand = new SpannableString(getString(R.string.pdf_studio));

        brand.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.upgrade_navy)
                ),
                0,
                3,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        brand.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.upgrade_gold)
                ),
                4,
                brand.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        heroBrandTitle.setText(brand);
    }

    private void setupBenefitItems() {
        configureBenefit(
                findViewById(R.id.benefitNoAds),
                R.drawable.ic_premium_no_ads,
                R.drawable.bg_benefit_icon_green,
                R.string.benefit_no_ads,
                R.string.benefit_no_ads_desc
        );

        configureBenefit(
                findViewById(R.id.benefitUnlimited),
                R.drawable.ic_premium_infinity,
                R.drawable.bg_benefit_icon_blue,
                R.string.benefit_unlimited,
                R.string.benefit_unlimited_desc
        );

        configureBenefit(
                findViewById(R.id.benefitAdvanced),
                R.drawable.ic_premium_tools,
                R.drawable.bg_benefit_icon_purple,
                R.string.benefit_advanced,
                R.string.benefit_advanced_desc
        );

        configureBenefit(
                findViewById(R.id.benefitRestrictions),
                R.drawable.ic_premium_shield,
                R.drawable.bg_benefit_icon_gold,
                R.string.benefit_restrictions,
                R.string.benefit_restrictions_desc
        );
    }

    private void configureBenefit(
            View item,
            int iconRes,
            int backgroundRes,
            int titleRes,
            int descriptionRes
    ) {
        if (item == null) {
            return;
        }

        FrameLayoutHolder holder = new FrameLayoutHolder(item);

        holder.iconContainer.setBackgroundResource(backgroundRes);
        holder.icon.setImageResource(iconRes);
        holder.title.setText(titleRes);
        holder.description.setText(descriptionRes);
    }

    private void setupPlanSelection() {
        View.OnClickListener monthlyListener = v -> {
            selectedPlan = Plan.MONTHLY;
            updatePlanUI();
        };

        View.OnClickListener yearlyListener = v -> {
            selectedPlan = Plan.YEARLY;
            updatePlanUI();
        };

        monthlyPlanCard.setOnClickListener(monthlyListener);
        yearlyPlanCard.setOnClickListener(yearlyListener);

        // The cards are the click targets. Radio buttons are visual state controls.
        monthlyRadio.setOnClickListener(monthlyListener);
        yearlyRadio.setOnClickListener(yearlyListener);
    }

    private void updatePlanUI() {
        boolean monthlySelected = selectedPlan == Plan.MONTHLY;

        monthlyRadio.setChecked(monthlySelected);
        yearlyRadio.setChecked(!monthlySelected);

        int selectedBorder = ContextCompat.getColor(this, R.color.upgrade_blue);
        int defaultBorder = ContextCompat.getColor(this, R.color.upgrade_card_border);
        int selectedBackground = ContextCompat.getColor(this, R.color.upgrade_background);
        int white = ContextCompat.getColor(this, R.color.white);

        updatePlanCard(
                monthlyPlanCard,
                monthlySelected,
                selectedBorder,
                defaultBorder,
                selectedBackground,
                white
        );

        updatePlanCard(
                yearlyPlanCard,
                !monthlySelected,
                selectedBorder,
                defaultBorder,
                selectedBackground,
                white
        );

        /*
         * These are intentionally isolated placeholder prices because the
         * supplied project has no BillingClient/ProductDetails implementation.
         * When Billing is added, set these TextViews from ProductDetails pricing.
         */
        monthlyPrice.setText(getString(R.string.monthly_placeholder));
        yearlyPrice.setText(getString(R.string.yearly_placeholder));

        if (monthlySelected) {
            heroPrice.setText(R.string.monthly_placeholder_price);
            heroPeriod.setText(R.string.per_month);
        } else {
            heroPrice.setText(R.string.yearly_placeholder_price);
            heroPeriod.setText(R.string.per_year);
        }
    }

    private void updatePlanCard(
            MaterialCardView card,
            boolean selected,
            int selectedBorder,
            int defaultBorder,
            int selectedBackground,
            int white
    ) {
        card.setStrokeColor(selected ? selectedBorder : defaultBorder);
        card.setStrokeWidth(selected ? dpToPx(2) : dpToPx(1));
        card.setCardBackgroundColor(selected ? selectedBackground : white);
        card.setCardElevation(selected ? dpToPx(3) : dpToPx(1));
    }

    private void setupUpgradeButton() {
        upgradeNowButton.setOnClickListener(v -> startPurchaseFlow());
    }

    private void setupRestorePurchase() {
        restorePurchaseButton.setOnClickListener(v -> restorePurchase());
    }

    private void setupLegalLinks() {

        TextView termsOfService = findViewById(R.id.termsOfService);
        TextView privacyPolicy = findViewById(R.id.privacyPolicy);

        termsOfService.setOnClickListener(v -> {
            if (TERMS_OF_SERVICE_URL.isEmpty()) {
                Toast.makeText(
                        this,
                        "Terms of Service URL is not configured yet.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            openUrl(TERMS_OF_SERVICE_URL);
        });

        // The supplied project already has a PrivacyPolicy Activity.
        privacyPolicy.setOnClickListener(v -> {

            Intent intent = new Intent(this, PrivacyPolicy.class);
            startActivity(intent);

        });
    }

    private void startPurchaseFlow() {
        /*
         * TODO: Google Play Billing integration.
         *
         * Use selectedPlan to choose the already-configured product:
         *
         * MONTHLY -> existing monthly ProductDetails
         * YEARLY  -> existing yearly ProductDetails
         *
         * Then launch the existing BillingClient purchase flow.
         *
         * The supplied Java source contains no BillingClient, ProductDetails,
         * subscription product IDs, or purchase handling, so this screen
         * intentionally does not fake a purchase.
         */
        String planName = selectedPlan == Plan.MONTHLY ? "Monthly" : "Yearly";

        Toast.makeText(
                this,
                planName + " plan selected. Billing is not configured yet.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void restorePurchase() {
        /*
         * TODO: Connect this method to the project's BillingClient query/
         * restore-purchases logic once Google Play Billing is implemented.
         */
        Toast.makeText(
                this,
                "Restore Purchase is not configured yet.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void openUrl(@NonNull String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Unable to open the link.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    /**
     * Small helper used to keep the benefit configuration readable.
     */
    private static class FrameLayoutHolder {
        final View iconContainer;
        final ImageView icon;
        final TextView title;
        final TextView description;

        FrameLayoutHolder(View root) {
            iconContainer = root.findViewById(R.id.benefitIconContainer);
            icon = root.findViewById(R.id.benefitIcon);
            title = root.findViewById(R.id.benefitTitle);
            description = root.findViewById(R.id.benefitDescription);
        }
    }
}
