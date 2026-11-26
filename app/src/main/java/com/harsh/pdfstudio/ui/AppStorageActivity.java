package com.harsh.pdfstudio.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.storage.AppStorageManager;
import com.harsh.pdfstudio.databinding.FragmentAppStorageBinding;
import com.harsh.pdfstudio.storage.ClearStorageDialogHelper;
import com.harsh.pdfstudio.storage.StorageDonutView;

import java.util.ArrayList;
import java.util.List;


public class AppStorageActivity extends AppCompatActivity {

    private FragmentAppStorageBinding binding;
    private AppStorageManager storageManager;

    /** Convenience launcher, mirroring the old Fragment's newInstance() factory. */
    public static Intent newIntent(Context context) {

        return new Intent(context, AppStorageActivity.class);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentAppStorageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageManager = new AppStorageManager(this);

        binding.toolbarAppStorage.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        binding.toolbarAppStorage.setOnMenuItemClickListener(item -> {
            // Hook up overflow actions (e.g. "Manage storage settings") here if needed.
            return true;
        });

        binding.btnClearAppStorage.setOnClickListener(v -> showClearStorageDialog());

        refreshStorageUsage(true);
    }


    private void refreshStorageUsage(boolean runCleanupFirst) {

        if (runCleanupFirst) {

            storageManager.runAutomaticCleanup(deletedCount ->
                    storageManager.calculateBreakdown(this::bindBreakdown));

        } else {

            storageManager.calculateBreakdown(this::bindBreakdown);

        }
    }

    private void bindBreakdown(AppStorageManager.StorageBreakdown breakdown) {
        Context ctx = this;

        String totalLabel = storageManager.formatBytes(breakdown.getTotalBytes());
        String pdfLabel = storageManager.formatBytes(breakdown.pdfBytes);
        String cacheLabel = storageManager.formatBytes(breakdown.cacheBytes);
        String otherLabel = storageManager.formatBytes(breakdown.otherBytes);

        float pdfPct = breakdown.percentageOf(breakdown.pdfBytes);
        float cachePct = breakdown.percentageOf(breakdown.cacheBytes);
        float otherPct = breakdown.percentageOf(breakdown.otherBytes);

        // Storage used card
        binding.tvStorageAmount.setText(totalLabel);

        // Breakdown rows
        binding.tvPdfStorage.setText(pdfLabel);
        binding.tvPdfPercentage.setText(storageManager.formatPercentage(pdfPct));

        binding.tvCacheStorage.setText(cacheLabel);
        binding.tvCachePercentage.setText(storageManager.formatPercentage(cachePct));

        binding.tvOtherStorage.setText(otherLabel);
        binding.tvOtherPercentage.setText(storageManager.formatPercentage(otherPct));

        binding.tvTotalStorage.setText(totalLabel);

        // Donut chart segments
        List<StorageDonutView.Segment> segments = new ArrayList<>();
        segments.add(new StorageDonutView.Segment(
                pdfPct, ContextCompat.getColor(ctx, R.color.blue_checkBox)));
        segments.add(new StorageDonutView.Segment(
                cachePct, ContextCompat.getColor(ctx, R.color.storage_green_500)));
        segments.add(new StorageDonutView.Segment(
                otherPct, ContextCompat.getColor(ctx, R.color.storage_orange_500)));

        binding.progressStorage.setSegments(segments);
    }

    private void showClearStorageDialog() {

        AppStorageManager storageManager = new AppStorageManager(this);

        storageManager.calculateBreakdown(breakdown -> {

            ClearStorageDialogHelper.show(
                    this,
                    storageManager,
                    breakdown,
                    (clearGeneratedPdfs, clearCache, clearOther) -> {

                        storageManager.clearSelectedStorage(
                                clearGeneratedPdfs,
                                clearCache,
                                clearOther,
                                success -> {

                                    if (success) {

                                        // Refresh storage information
                                        refreshStorageUsage(true);

                                    } else {

                                        // Show failure message
                                        Toast.makeText(
                                                this,
                                                "Failed to clear some storage",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    storageManager.shutdown();
                                }
                        );
                    }
            );
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (storageManager != null) {
            storageManager.shutdown();
        }
        binding = null;
    }
}
