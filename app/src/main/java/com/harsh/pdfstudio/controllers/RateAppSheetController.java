package com.harsh.pdfstudio.controllers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.harsh.pdfstudio.R;

/**
 * Wires up and drives the "Rate PDF Studio" sheet that lives inline inside
 * home_fragment.xml (View.VISIBLE / View.GONE, not a Dialog or Fragment).
 *
 * Usage from HomeFragment:
 *
 *     private RateAppSheetController rateAppSheetController;
 *
 *     @Override
 *     public void onViewCreated(View view, Bundle savedInstanceState) {
 *         rateAppSheetController = new RateAppSheetController(view);
 *         rateAppSheetController.setOnLowRatingListener(rating ->
 *                 new YourFeedbackDialog(rating).show(getChildFragmentManager(), "feedback"));
 *
 *         View rateApp = view.findViewById(R.id.item_rate); // wherever "Rate App" is triggered from
 *         rateApp.setOnClickListener(v -> rateAppSheetController.show());
 *     }
 */
public class RateAppSheetController {

    /** Called when the user picks 1–3 stars and taps "Rate PDF Studio". */
    public interface OnLowRatingListener {
        void onLowRatingSelected(int rating);
    }

    private final Context context;
    private final View rateAppSheet;
    private final ImageView ivClose;
    private final TextView tvMaybeLater;
    private final TextView tvRatingLabel;
    private final MaterialButton btnRate;
    private final ImageView[] stars;

    private int selectedRating = 0;

    @Nullable
    private OnLowRatingListener lowRatingListener;

    /**
     * @param rootView any view that contains the rate-app sheet in its hierarchy —
     *                 typically the Fragment's root view from onViewCreated().
     */
    public RateAppSheetController(@NonNull View rootView) {
        this.context = rootView.getContext();

        rateAppSheet = rootView.findViewById(R.id.rateApp);
        ivClose = rootView.findViewById(R.id.iv_close);
        tvMaybeLater = rootView.findViewById(R.id.tv_maybe_later);
        tvRatingLabel = rootView.findViewById(R.id.tv_rating_label);
        btnRate = rootView.findViewById(R.id.btn_rate);

        stars = new ImageView[]{
                rootView.findViewById(R.id.star_1),
                rootView.findViewById(R.id.star_2),
                rootView.findViewById(R.id.star_3),
                rootView.findViewById(R.id.star_4),
                rootView.findViewById(R.id.star_5)
        };

        setupListeners();
    }

    public void setOnLowRatingListener(@Nullable OnLowRatingListener listener) {
        this.lowRatingListener = listener;
    }

    /** Call this from wherever "Rate App" is tapped (drawer item, menu, etc.). */
    public void show() {
        resetUi();
        rateAppSheet.setVisibility(View.VISIBLE);
    }

    public void hide() {
        rateAppSheet.setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return rateAppSheet.getVisibility() == View.VISIBLE;
    }

    // -------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------

    private void setupListeners() {
        for (int i = 0; i < stars.length; i++) {
            int rating = i + 1;
            stars[i].setOnClickListener(v -> onStarSelected(rating));
        }

        ivClose.setOnClickListener(v -> hide());
        tvMaybeLater.setOnClickListener(v -> hide());

        btnRate.setOnClickListener(v -> {
            if (selectedRating == 0) return;

            if (selectedRating >= 4) {
                openPlayStoreListing();
                hide();
            } else {
                int rating = selectedRating;
                hide();
                if (lowRatingListener != null) {
                    lowRatingListener.onLowRatingSelected(rating);
                }
            }
        });
    }

    // -------------------------------------------------------------------
    // UI state
    // -------------------------------------------------------------------

    private void resetUi() {
        selectedRating = 0;
        btnRate.setEnabled(false);
        tvRatingLabel.setText(R.string.rate_label_default);
        updateStarIcons(0);
    }

    private void onStarSelected(int rating) {
        selectedRating = rating;
        updateStarIcons(rating);
        updateRatingLabel(rating);
        btnRate.setEnabled(true);
    }

    private void updateStarIcons(int rating) {
        int selectedColor = ContextCompat.getColor(context, R.color.rate_star_selected);
        int unselectedColor = ContextCompat.getColor(context, R.color.rate_star_unselected);

        for (int i = 0; i < stars.length; i++) {
            boolean filled = (i + 1) <= rating;
            stars[i].setImageResource(filled ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            stars[i].setImageTintList(ColorStateList.valueOf(filled ? selectedColor : unselectedColor));
        }
    }

    private void updateRatingLabel(int rating) {
        String label = (rating == 1)
                ? context.getString(R.string.rate_label_one_star)
                : context.getString(R.string.rate_label_n_stars, rating);
        tvRatingLabel.setText(label);
    }

    // -------------------------------------------------------------------
    // Play Store
    // -------------------------------------------------------------------

    private void openPlayStoreListing() {
        String pkg = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }
    }
}
