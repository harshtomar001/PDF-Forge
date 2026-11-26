package com.harsh.pdfstudio.storage;

// --- Required imports -------------------------------------------------
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.harsh.pdfstudio.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public final class ClearStorageDialogHelper {

    public interface OnClearConfirmedListener {
        void onClearConfirmed(boolean clearGeneratedPdfs, boolean clearCache, boolean clearOther);
    }



    public static void show(
            @NonNull Context context,
            @NonNull AppStorageManager storageManager,
            @NonNull AppStorageManager.StorageBreakdown breakdown,
            @NonNull OnClearConfirmedListener listener
    ) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_clear_storage, null, false);

        // --- Bind views -----------------------------------------------
        LinearLayout rowGeneratedPdfs = dialogView.findViewById(R.id.rowGeneratedPdfs);
        LinearLayout rowCache = dialogView.findViewById(R.id.rowCache);
        LinearLayout rowOther = dialogView.findViewById(R.id.rowOther);

        MaterialCheckBox checkGeneratedPdfs = dialogView.findViewById(R.id.checkGeneratedPdfs);
        MaterialCheckBox checkCache = dialogView.findViewById(R.id.checkCache);
        MaterialCheckBox checkOther = dialogView.findViewById(R.id.checkOther);

        TextView textPdfSize = dialogView.findViewById(R.id.textPdfSize);
        TextView textCacheSize = dialogView.findViewById(R.id.textCacheSize);
        TextView textOtherSize = dialogView.findViewById(R.id.textOtherSize);

        TextView textSelectionError = dialogView.findViewById(R.id.textSelectionError);

        MaterialButton buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        MaterialButton buttonClear = dialogView.findViewById(R.id.buttonClear);

        // --- Populate sizes dynamically (never hardcoded) --------------
        textPdfSize.setText(storageManager.formatBytes(breakdown.pdfBytes));
        textCacheSize.setText(storageManager.formatBytes(breakdown.cacheBytes));
        textOtherSize.setText(storageManager.formatBytes(breakdown.otherBytes));

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] {}
        };

        int[] colors = new int[] {
                ContextCompat.getColor(context, R.color.blue_checkBox),
                ContextCompat.getColor(context, R.color.grey)
        };

        ColorStateList colorStateList =
                new ColorStateList(states, colors);

        checkGeneratedPdfs.setButtonTintList(colorStateList);
        checkCache.setButtonTintList(colorStateList);
        checkOther.setButtonTintList(colorStateList);

        // --- Default checked state per spec -----------------------------
        checkGeneratedPdfs.setChecked(false);
        checkCache.setChecked(true);
        checkOther.setChecked(false);

        // --- Make each entire row clickable, not just the checkbox -----
        rowGeneratedPdfs.setOnClickListener(v ->
                toggleAndClearError(checkGeneratedPdfs, textSelectionError));

        rowCache.setOnClickListener(v ->
                toggleAndClearError(checkCache, textSelectionError));

        rowOther.setOnClickListener(v ->
                toggleAndClearError(checkOther, textSelectionError));

        // Also clear the error if the user taps a checkbox directly.
        checkGeneratedPdfs.setOnCheckedChangeListener((b, isChecked) ->
                textSelectionError.setVisibility(View.GONE));
        checkCache.setOnCheckedChangeListener((b, isChecked) ->
                textSelectionError.setVisibility(View.GONE));
        checkOther.setOnCheckedChangeListener((b, isChecked) ->
                textSelectionError.setVisibility(View.GONE));


        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonClear.setOnClickListener(v -> {

            boolean clearGeneratedPdfs = checkGeneratedPdfs.isChecked();

            boolean clearCache = checkCache.isChecked();

            boolean clearOther = checkOther.isChecked();

            if (!clearGeneratedPdfs && !clearCache && !clearOther) {
                // Requirement 7: show inline error, do NOT close the dialog.
                textSelectionError.setVisibility(View.VISIBLE);

                return;
            }

            dialog.dismiss();
            listener.onClearConfirmed(clearGeneratedPdfs, clearCache, clearOther);

        });

        dialog.show();
    }

    private static void toggleAndClearError(MaterialCheckBox checkBox, TextView errorText) {

        checkBox.setChecked(!checkBox.isChecked());
        errorText.setVisibility(View.GONE);

    }
}
