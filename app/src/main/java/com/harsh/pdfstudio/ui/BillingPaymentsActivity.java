package com.harsh.pdfstudio.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.PaymentHistoryAdapter;
import com.harsh.pdfstudio.model.PaymentHistoryItem;

import java.util.ArrayList;
import java.util.List;


public class BillingPaymentsActivity extends AppCompatActivity {

    /** Swap this with your real billing-repository call. */
    private static final boolean DEBUG_FORCE_NO_SUBSCRIPTION = false;

    private MaterialCardView cardPaymentMethodActive;
    private LinearLayout groupPaymentMethodEmpty;
    private TextView txtPaymentMethodSecurityLine1;
    private TextView txtCardLast4;

    private TextView txtSubscriptionStatus;
    private TextView txtLastPayment;
    private TextView txtLastPaymentDate;
    private TextView txtNextPayment;
    private TextView txtNextBillingDate;

    private MaterialCardView cardPaymentHistoryList;
    private MaterialCardView cardPaymentHistoryEmpty;
    private RecyclerView recyclerPaymentHistory;
    private MaterialButton btnUpgrade;
    private TextView btnViewAll;

    private LinearLayout groupRecentInvoice;
    private TextView txtInvoiceTitle;
    private TextView txtInvoiceMeta;
    private TextView txtInvoiceAmount;
    private MaterialButton btnViewReceipt;
    private MaterialButton btnDownloadInvoice;

    private LinearLayout groupPremiumPromo;

    private ImageView btnBack;
    private ImageView btnHelp;
    private LinearLayout btnManage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_payments);

        bindViews();
        setUpClickListeners();

        BillingState state = loadBillingState();
        render(state);
    }

    private void bindViews() {

        cardPaymentMethodActive = findViewById(R.id.cardPaymentMethodActive);
        groupPaymentMethodEmpty = findViewById(R.id.groupPaymentMethodEmpty);
        txtPaymentMethodSecurityLine1 = findViewById(R.id.txtPaymentMethodSecurityLine1);
        txtCardLast4 = findViewById(R.id.txtCardLast4);

        txtSubscriptionStatus = findViewById(R.id.txtSubscriptionStatus);
        txtLastPayment = findViewById(R.id.txtLastPayment);
        txtLastPaymentDate = findViewById(R.id.txtLastPaymentDate);
        txtNextPayment = findViewById(R.id.txtNextPayment);
        txtNextBillingDate = findViewById(R.id.txtNextBillingDate);

        cardPaymentHistoryList = findViewById(R.id.cardPaymentHistoryList);
        cardPaymentHistoryEmpty = findViewById(R.id.cardPaymentHistoryEmpty);
        recyclerPaymentHistory = findViewById(R.id.recyclerPaymentHistory);
        btnUpgrade = findViewById(R.id.btnUpgrade);
        btnViewAll = findViewById(R.id.btnViewAll);

        groupRecentInvoice = findViewById(R.id.groupRecentInvoice);
        txtInvoiceTitle = findViewById(R.id.txtInvoiceTitle);
        txtInvoiceMeta = findViewById(R.id.txtInvoiceMeta);
        txtInvoiceAmount = findViewById(R.id.txtInvoiceAmount);
        btnViewReceipt = findViewById(R.id.btnViewReceipt);
        btnDownloadInvoice = findViewById(R.id.btnDownloadInvoice);

        groupPremiumPromo = findViewById(R.id.groupPremiumPromo);

        btnBack = findViewById(R.id.btnBack);
        btnHelp = findViewById(R.id.btnHelp);
        btnManage = findViewById(R.id.btnManage);

        bindPromoRow(R.id.rowBenefit1, R.string.promo_benefit_1);
        bindPromoRow(R.id.rowBenefit2, R.string.promo_benefit_2);
        bindPromoRow(R.id.rowBenefit3, R.string.promo_benefit_3);
        bindPromoRow(R.id.rowBenefit4, R.string.promo_benefit_4);
        bindPromoRow(R.id.rowBenefit5, R.string.promo_benefit_5);
    }

    private void bindPromoRow(int includeId, int stringRes) {

        View row = findViewById(includeId);
        TextView label = row.findViewById(R.id.txtBenefit);
        label.setText(stringRes);

    }

    private void setUpClickListeners() {

        btnBack.setOnClickListener(v -> onBackPressed());

        btnHelp.setOnClickListener(v ->
                Toast.makeText(this, "Open billing help", Toast.LENGTH_SHORT).show());

        btnManage.setOnClickListener(v ->
                openGooglePlaySubscriptionManagement());

        btnUpgrade.setOnClickListener(v -> {
            // TODO: point this at your Subscription/Paywall screen, e.g.
            // startActivity(new android.content.Intent(this, SubscriptionActivity.class));
            Toast.makeText(this, "Open subscription screen", Toast.LENGTH_SHORT).show();
        });

        btnViewAll.setOnClickListener(v ->
                Toast.makeText(this, "Open full payment history", Toast.LENGTH_SHORT).show());

        btnViewReceipt.setOnClickListener(v ->
                Toast.makeText(this, "Open receipt", Toast.LENGTH_SHORT).show());

        btnDownloadInvoice.setOnClickListener(v ->
                Toast.makeText(this, "Download invoice", Toast.LENGTH_SHORT).show());
    }

    /**
     * Replace with your real data source: BillingClient / your backend /
     * a cached repository. This method is the single seam that decides
     * which of the two screen states renders.
     */
    private BillingState loadBillingState() {

        if (DEBUG_FORCE_NO_SUBSCRIPTION) {
            return BillingState.noSubscription();
        }
        return BillingState.activeSample();
    }

    private void render(BillingState state) {

        boolean hasActiveSubscription = state.hasActiveSubscription;

        renderPaymentMethod(state, hasActiveSubscription);
        renderBillingSummary(state, hasActiveSubscription);
        renderPaymentHistory(state, hasActiveSubscription);

        groupRecentInvoice.setVisibility(hasActiveSubscription ? View.VISIBLE : View.GONE);
        if (hasActiveSubscription) {
            txtInvoiceTitle.setText(state.invoiceTitle);
            txtInvoiceMeta.setText(getString(R.string.invoice_meta_format, state.invoiceDate, state.invoiceNumber));
            txtInvoiceAmount.setText(state.invoiceAmount);
        }

        groupPremiumPromo.setVisibility(hasActiveSubscription ? View.GONE : View.VISIBLE);
    }

    private void renderPaymentMethod(BillingState state, boolean active) {

        cardPaymentMethodActive.setVisibility(active ? View.VISIBLE : View.GONE);
        groupPaymentMethodEmpty.setVisibility(active ? View.GONE : View.VISIBLE);

        if (active) {
            txtCardLast4.setText(state.cardLast4Label);
            txtPaymentMethodSecurityLine1.setText(R.string.security_payment_method_line1);
        } else {
            txtPaymentMethodSecurityLine1.setText(R.string.security_payment_method_line1_empty);
        }
    }

    private void renderBillingSummary(BillingState state, boolean active) {

        if (active) {

            txtSubscriptionStatus.setText(R.string.status_active);
            txtSubscriptionStatus.setTextColor(getThemeColor(R.color.colorSuccess));
            txtLastPayment.setText(state.lastPaymentAmount);
            txtLastPaymentDate.setText(state.lastPaymentDate);
            txtNextPayment.setText(state.nextPaymentAmount);
            txtNextBillingDate.setText(state.nextBillingDate);

        }
        else {

            txtSubscriptionStatus.setText(R.string.status_none);
            txtSubscriptionStatus.setTextColor(getThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
            String dash = getString(R.string.value_placeholder);
            txtLastPayment.setText(dash);
            txtLastPaymentDate.setText(dash);
            txtNextPayment.setText(dash);
            txtNextBillingDate.setText(dash);

        }

    }

    private void renderPaymentHistory(BillingState state, boolean active) {

        boolean hasHistory = active && !state.paymentHistory.isEmpty();

        cardPaymentHistoryList.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
        cardPaymentHistoryEmpty.setVisibility(hasHistory ? View.GONE : View.VISIBLE);
        btnViewAll.setVisibility(hasHistory ? View.VISIBLE : View.GONE);

        if (hasHistory) {

            recyclerPaymentHistory.setLayoutManager(new LinearLayoutManager(this));
            recyclerPaymentHistory.setAdapter(new PaymentHistoryAdapter(state.paymentHistory,
                    item -> Toast.makeText(this,
                            "Open transaction " + item.getOrderId(),
                            Toast.LENGTH_SHORT).show()));
        }
    }

    private void openGooglePlaySubscriptionManagement() {

        try {

            String packageName = getPackageName();
            android.content.Intent intent = new android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://play.google.com/store/account/subscriptions?package=" + packageName));
            startActivity(intent);

        }
        catch (Exception e) {

            Toast.makeText(this, "Couldn't open Google Play", Toast.LENGTH_SHORT).show();

        }

    }

    private int getThemeColor(int attrRes) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.data;
    }

    /**
     * Plain data holder for everything the screen needs to render.
     * Replace the sample factories with your real repository/API mapping.
     */
    private static class BillingState {
        boolean hasActiveSubscription;
        String cardLast4Label;
        String lastPaymentAmount;
        String lastPaymentDate;
        String nextPaymentAmount;
        String nextBillingDate;
        String invoiceTitle;
        String invoiceAmount;
        String invoiceDate;
        String invoiceNumber;
        List<PaymentHistoryItem> paymentHistory = new ArrayList<>();

        static BillingState activeSample() {

            BillingState s = new BillingState();

            s.hasActiveSubscription = true;
            s.cardLast4Label = "Visa •••• 4821";
            s.lastPaymentAmount = "₹299";
            s.lastPaymentDate = "30 Aug 2026";
            s.nextPaymentAmount = "₹299";
            s.nextBillingDate = "30 Sep 2026";
            s.invoiceTitle = "PDF Studio Premium Monthly";
            s.invoiceAmount = "₹299.00";
            s.invoiceDate = "30 August 2026";
            s.invoiceNumber = "INV-20260830-001";

            s.paymentHistory.add(new PaymentHistoryItem(
                    "30 Aug 2026", "PDF Studio Premium Monthly",
                    "GPA.3387-1928-7629-12345", "₹299.00",
                    PaymentHistoryItem.PaymentStatus.PAID));
            s.paymentHistory.add(new PaymentHistoryItem(
                    "30 Jul 2026", "PDF Studio Premium Monthly",
                    "GPA.3376-1827-6528-11234", "₹299.00",
                    PaymentHistoryItem.PaymentStatus.PAID));
            s.paymentHistory.add(new PaymentHistoryItem(
                    "30 Jun 2026", "PDF Studio Premium Monthly",
                    "GPA.3365-1726-5427-10123", "₹299.00",
                    PaymentHistoryItem.PaymentStatus.PAID));
            return s;
        }

        static BillingState noSubscription() {
            BillingState s = new BillingState();
            s.hasActiveSubscription = false;
            return s;
        }

    }
}
