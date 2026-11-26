package com.harsh.pdfstudio.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.harsh.pdfstudio.BuildConfig;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// NOTE: adjust these two imports to match your app's real namespace/applicationId.
// ViewBinding generates FragmentSettingsBinding from fragment_settings.xml and
// ItemSettingRowBinding from item_setting_row.xml under "<namespace>.databinding".
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.databinding.FragmentSettingsBinding;
import com.harsh.pdfstudio.databinding.ItemSettingRowBinding;
import com.harsh.pdfstudio.managers.SettingsManager;


public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsManager settingsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        settingsManager = new SettingsManager(requireContext());

        setupAppearanceSection();
        setupPdfSettingsSection();
        setupStorageSection();
        setupNotificationsSection();
        setupPdfOperationsSection();
        setupAboutSection();
        setupResetSettings();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // avoid leaking the view tree
    }



    private void setupAppearanceSection() {

        refreshThemeRow();
        binding.rowTheme.rowRoot.setOnClickListener(v -> showThemeDialog());

        refreshAccentColorRow();
        binding.rowAccentColor.rowRoot.setOnClickListener(v -> showAccentColorDialog());

    }

    private void refreshThemeRow() {

        setupValueRow(binding.rowTheme, R.drawable.ic_dark_mode,
                R.string.row_theme_title, R.string.row_theme_desc,
                getString(themeLabelRes(settingsManager.getTheme())));

    }

    private void showThemeDialog() {

        SettingsManager.Theme[] values = SettingsManager.Theme.values();
        String[] labels = new String[values.length];

        int checked = 0;

        for (int i = 0; i < values.length; i++) {

            labels[i] = getString(themeLabelRes(values[i]));
            if (values[i] == settingsManager.getTheme()) checked = i;

        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_theme_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    settingsManager.setTheme(values[which]);
                    refreshThemeRow();
                    dialog.dismiss();
                    // TODO: apply AppCompatDelegate.setDefaultNightMode(...) here if this
                    // screen should also switch the theme immediately.
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();

    }

    @StringRes
    private int themeLabelRes(SettingsManager.Theme theme) {

        switch (theme) {
            case LIGHT: return R.string.theme_light;
            case DARK: return R.string.theme_dark;
            case SYSTEM_DEFAULT:
            default: return R.string.theme_system_default;
        }

    }

    private void refreshAccentColorRow() {

        setupColorRow(binding.rowAccentColor, R.drawable.ic_color_lens,
                R.string.row_accent_color_title, R.string.row_accent_color_desc,
                accentColorInt(settingsManager.getAccentColor()));

    }

    private void showAccentColorDialog() {

        SettingsManager.AccentColor[] values = SettingsManager.AccentColor.values();
        String[] labels = new String[values.length];

        int checked = 0;

        for (int i = 0; i < values.length; i++) {

            labels[i] = getString(accentColorLabelRes(values[i]));
            if (values[i] == settingsManager.getAccentColor()) checked = i;

        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_accent_color_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    settingsManager.setAccentColor(values[which]);
                    refreshAccentColorRow();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();

    }

    @StringRes
    private int accentColorLabelRes(SettingsManager.AccentColor color) {

        switch (color) {
            case BLUE: return R.string.accent_color_blue;
            case GREEN: return R.string.accent_color_green;
            case ORANGE: return R.string.accent_color_orange;
            case PURPLE:
            default: return R.string.accent_color_purple;
        }

    }

    @ColorInt
    private int accentColorInt(SettingsManager.AccentColor color) {

        int colorRes;

        switch (color) {
            case BLUE: colorRes = R.color.accent_blue; break;
            case GREEN: colorRes = R.color.accent_green; break;
            case ORANGE: colorRes = R.color.accent_orange; break;
            case PURPLE:
            default: colorRes = R.color.accent_purple; break;
        }
        return ContextCompat.getColor(requireContext(), colorRes);

    }

    // =====================================================================
    // PDF SETTINGS
    // =====================================================================

    private void setupPdfSettingsSection() {
        refreshPageSizeRow();
        binding.rowPageSize.rowRoot.setOnClickListener(v -> showPageSizeDialog());

        refreshPageMarginRow();
        binding.rowPageMargin.rowRoot.setOnClickListener(v -> showPageMarginDialog());

        setupSwitchRow(binding.rowRememberLastSettings, R.drawable.ic_history,
                R.string.row_remember_last_title, R.string.row_remember_last_desc,
                settingsManager.isRememberLastSettingsEnabled(),
                (btn, isChecked) -> settingsManager.setRememberLastSettingsEnabled(isChecked));
    }

    private void refreshPageSizeRow() {
        setupValueRow(binding.rowPageSize, R.drawable.ic_picture_as_pdf,
                R.string.row_page_size_title, R.string.row_page_size_desc,
                getString(pageSizeLabelRes(settingsManager.getDefaultPageSize())));
    }

    private void showPageSizeDialog() {
        SettingsManager.PageSize[] values = SettingsManager.PageSize.values();
        String[] labels = new String[values.length];
        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            labels[i] = getString(pageSizeLabelRes(values[i]));
            if (values[i] == settingsManager.getDefaultPageSize()) checked = i;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_page_size_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    settingsManager.setDefaultPageSize(values[which]);
                    refreshPageSizeRow();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @StringRes
    private int pageSizeLabelRes(SettingsManager.PageSize size) {
        switch (size) {
            case ORIGINAL: return R.string.page_size_original;
            case A3: return R.string.page_size_a3;
            case LETTER: return R.string.page_size_letter;
            case LEGAL: return R.string.page_size_legal;
            case A4:
            default: return R.string.page_size_a4;
        }
    }

    private void refreshPageMarginRow() {
        setupValueRow(binding.rowPageMargin, R.drawable.ic_border_style,
                R.string.row_page_margin_title, R.string.row_page_margin_desc,
                getString(marginLabelRes(settingsManager.getDefaultMargin())));
    }

    private void showPageMarginDialog() {
        SettingsManager.PageMargin[] values = SettingsManager.PageMargin.values();
        String[] labels = new String[values.length];
        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            labels[i] = getString(marginLabelRes(values[i]));
            if (values[i] == settingsManager.getDefaultMargin()) checked = i;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_page_margin_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    settingsManager.setDefaultMargin(values[which]);
                    refreshPageMarginRow();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @StringRes
    private int marginLabelRes(SettingsManager.PageMargin margin) {
        switch (margin) {
            case SMALL: return R.string.margin_small;
            case MEDIUM: return R.string.margin_medium;
            case LARGE: return R.string.margin_large;
            case NONE:
            default: return R.string.margin_none;
        }
    }

    // =====================================================================
    // STORAGE
    // =====================================================================

    private void setupStorageSection() {
        setupNavRow(binding.rowAppStorage, R.drawable.ic_storage,
                R.string.row_app_storage_title, R.string.row_app_storage_desc);
        binding.rowAppStorage.rowRoot.setOnClickListener(v -> openAppStorageScreen());

        setupSwitchRow(binding.rowAutoCleanup, R.drawable.ic_delete_sweep,
                R.string.row_auto_cleanup_title, R.string.row_auto_cleanup_desc,
                settingsManager.isAutomaticCleanupEnabled(),
                (btn, isChecked) -> {
                    settingsManager.setAutomaticCleanupEnabled(isChecked);
                    setCleanupAfterRowEnabled(isChecked);
                });

        refreshCleanupAfterRow();
        setCleanupAfterRowEnabled(settingsManager.isAutomaticCleanupEnabled());
        binding.rowCleanupAfter.rowRoot.setOnClickListener(v -> showCleanupDurationDialog());
    }

    /** Opens the app's existing storage-management screen. Do not build a new one here. */
    private void openAppStorageScreen() {
        // TODO: wire this to your existing App Storage screen, e.g.:
        //   Navigation.findNavController(requireView()).navigate(R.id.action_settings_to_appStorage);
        // or a FragmentTransaction / startActivity call, depending on how the rest
        // of PDF Studio navigates between screens.
    }

    private void refreshCleanupAfterRow() {
        setupValueRow(binding.rowCleanupAfter, R.drawable.ic_schedule,
                R.string.row_cleanup_after_title, R.string.row_cleanup_after_desc,
                getString(cleanupLabelRes(settingsManager.getCleanupDuration())));
    }

    private void showCleanupDurationDialog() {
        SettingsManager.CleanupDuration[] values = SettingsManager.CleanupDuration.values();
        String[] labels = new String[values.length];
        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            labels[i] = getString(cleanupLabelRes(values[i]));
            if (values[i] == settingsManager.getCleanupDuration()) checked = i;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_cleanup_after_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    settingsManager.setCleanupDuration(values[which]);
                    refreshCleanupAfterRow();
                    dialog.dismiss();
                    // AppStorageManager.AUTO_CLEANUP_MAX_AGE_MILLIS should read
                    // settingsManager.getCleanupDurationMillis() so this takes effect.
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @StringRes
    private int cleanupLabelRes(SettingsManager.CleanupDuration duration) {
        switch (duration) {
            case MIN_30: return R.string.cleanup_30_minutes;
            case HOUR_1: return R.string.cleanup_1_hour;
            case HOUR_6: return R.string.cleanup_6_hours;
            case HOUR_24: return R.string.cleanup_24_hours;
            case HOUR_2:
            default: return R.string.cleanup_2_hours;
        }
    }

    /** Dims and disables the "Cleanup After" row when Automatic Cleanup is off. */
    private void setCleanupAfterRowEnabled(boolean enabled) {
        binding.rowCleanupAfter.rowRoot.setEnabled(enabled);
        binding.rowCleanupAfter.rowRoot.setAlpha(enabled ? 1f : 0.4f);
    }

    // =====================================================================
    // NOTIFICATIONS
    // =====================================================================

    private void setupNotificationsSection() {
        setupSwitchRow(binding.rowPdfCompletionNotif, R.drawable.ic_notifications,
                R.string.row_pdf_completion_notif_title, R.string.row_pdf_completion_notif_desc,
                settingsManager.isPdfCompletionNotificationEnabled(),
                (btn, isChecked) -> settingsManager.setPdfCompletionNotificationEnabled(isChecked));

        setupSwitchRow(binding.rowErrorNotif, R.drawable.ic_error_outline,
                R.string.row_error_notif_title, R.string.row_error_notif_desc,
                settingsManager.isErrorNotificationEnabled(),
                (btn, isChecked) -> settingsManager.setErrorNotificationEnabled(isChecked));

        setupSwitchRow(binding.rowNotificationSound, R.drawable.ic_volume_up,
                R.string.row_notification_sound_title, R.string.row_notification_sound_desc,
                settingsManager.isNotificationSoundEnabled(),
                (btn, isChecked) -> settingsManager.setNotificationSoundEnabled(isChecked));
    }

    // =====================================================================
    // PDF OPERATIONS
    // =====================================================================

    private void setupPdfOperationsSection() {
        setupSwitchRow(binding.rowOpenAfterCreation, R.drawable.ic_picture_as_pdf,
                R.string.row_open_after_creation_title, R.string.row_open_after_creation_desc,
                settingsManager.isOpenPdfAfterCreationEnabled(),
                (btn, isChecked) -> settingsManager.setOpenPdfAfterCreationEnabled(isChecked));

        setupSwitchRow(binding.rowSaveGeneratedPdfs, R.drawable.ic_save,
                R.string.row_save_generated_title, R.string.row_save_generated_desc,
                settingsManager.isSaveGeneratedPdfsEnabled(),
                (btn, isChecked) -> settingsManager.setSaveGeneratedPdfsEnabled(isChecked));

        setupSwitchRow(binding.rowAskBeforeOverwrite, R.drawable.ic_warning,
                R.string.row_ask_overwrite_title, R.string.row_ask_overwrite_desc,
                settingsManager.isAskBeforeOverwriteEnabled(),
                (btn, isChecked) -> settingsManager.setAskBeforeOverwriteEnabled(isChecked));
    }

    // =====================================================================
    // ABOUT
    // =====================================================================

    private void setupAboutSection() {
        setupNavRow(binding.rowAboutApp, R.drawable.ic_info,
                R.string.row_about_app_title, R.string.row_about_app_desc);
        binding.rowAboutApp.rowRoot.setOnClickListener(v -> {
            // TODO: navigate to your existing "About" screen.
        });

        setupNavRow(binding.rowHelpSupport, R.drawable.ic_help_outline,
                R.string.row_help_support_title, R.string.row_help_support_desc);
        binding.rowHelpSupport.rowRoot.setOnClickListener(v -> {
            // TODO: navigate to your existing Help & Support screen.
        });

        setupNavRow(binding.rowTerms, R.drawable.ic_description,
                R.string.row_terms_title, R.string.row_terms_desc);
        binding.rowTerms.rowRoot.setOnClickListener(v -> {
            // TODO: navigate to Terms & Services (in-app screen or a browser Intent).
        });

        setupNavRow(binding.rowPrivacyPolicy, R.drawable.ic_privacy_tip,
                R.string.row_privacy_title, R.string.row_privacy_desc);
        binding.rowPrivacyPolicy.rowRoot.setOnClickListener(v -> {
            // TODO: navigate to Privacy Policy (in-app screen or a browser Intent).
        });

        // Static, non-interactive: show BuildConfig.VERSION_NAME, never a hardcoded string.

        setupValueRow(binding.rowAppVersion, R.drawable.ic_info_outline,
                R.string.row_app_version_title, R.string.row_app_version_desc,
                BuildConfig.VERSION_NAME);

        binding.rowAppVersion.rowRoot.setClickable(false);
        binding.rowAppVersion.rowRoot.setFocusable(false);
    }

    // =====================================================================
    // RESET SETTINGS
    // =====================================================================

    private void setupResetSettings() {

        binding.resetSettingsRow.setOnClickListener(v -> showResetConfirmationDialog());

    }

    private void showResetConfirmationDialog() {

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset_dialog_title)
                .setMessage(R.string.reset_dialog_message)
                .setNegativeButton(R.string.reset_dialog_cancel, null)
                .setPositiveButton(R.string.reset_dialog_confirm, (dialog, which) -> {
                    settingsManager.resetToDefaults();
                    reloadAllRows();
                    Toast.makeText(requireContext(),
                            R.string.reset_settings_confirmation_toast, Toast.LENGTH_SHORT).show();
                })
                .show();

    }

    /** Re-reads every value from SettingsManager and refreshes the whole screen after a reset. */
    private void reloadAllRows() {

        refreshThemeRow();
        refreshAccentColorRow();
        refreshPageSizeRow();
        refreshPageMarginRow();
        binding.rowRememberLastSettings.rowSwitch.setChecked(settingsManager.isRememberLastSettingsEnabled());

        binding.rowAutoCleanup.rowSwitch.setChecked(settingsManager.isAutomaticCleanupEnabled());
        refreshCleanupAfterRow();
        setCleanupAfterRowEnabled(settingsManager.isAutomaticCleanupEnabled());

        binding.rowPdfCompletionNotif.rowSwitch.setChecked(settingsManager.isPdfCompletionNotificationEnabled());
        binding.rowErrorNotif.rowSwitch.setChecked(settingsManager.isErrorNotificationEnabled());
        binding.rowNotificationSound.rowSwitch.setChecked(settingsManager.isNotificationSoundEnabled());

        binding.rowOpenAfterCreation.rowSwitch.setChecked(settingsManager.isOpenPdfAfterCreationEnabled());
        binding.rowSaveGeneratedPdfs.rowSwitch.setChecked(settingsManager.isSaveGeneratedPdfsEnabled());
        binding.rowAskBeforeOverwrite.rowSwitch.setChecked(settingsManager.isAskBeforeOverwriteEnabled());
    }

    // =====================================================================
    // ROW HELPERS -- these keep every card's setup code above to one line
    // per row instead of repeating this boilerplate seven sections in a row.
    // =====================================================================

    private void setupValueRow(ItemSettingRowBinding row, @DrawableRes int icon,
                                @StringRes int title, @StringRes int desc, String value) {
        row.rowIcon.setImageResource(icon);
        row.rowTitle.setText(title);
        row.rowDescription.setText(desc);
        row.rowValue.setText(value);
        row.rowValue.setVisibility(View.VISIBLE);
        row.rowSwitch.setVisibility(View.GONE);
        row.rowChevron.setVisibility(View.GONE);
        row.rowColorDot.setVisibility(View.GONE);
    }

    private void setupSwitchRow(ItemSettingRowBinding row, @DrawableRes int icon,
                                 @StringRes int title, @StringRes int desc, boolean checked,
                                 android.widget.CompoundButton.OnCheckedChangeListener listener) {
        row.rowIcon.setImageResource(icon);
        row.rowTitle.setText(title);
        row.rowDescription.setText(desc);
        row.rowValue.setVisibility(View.GONE);
        row.rowChevron.setVisibility(View.GONE);
        row.rowColorDot.setVisibility(View.GONE);
        row.rowSwitch.setVisibility(View.VISIBLE);
        row.rowSwitch.setOnCheckedChangeListener(null); // avoid firing while we set state
        row.rowSwitch.setChecked(checked);
        row.rowSwitch.setOnCheckedChangeListener(listener);
        // Tapping anywhere on the row toggles it too, not just the switch itself.
        row.rowRoot.setOnClickListener(v -> row.rowSwitch.toggle());
    }

    private void setupNavRow(ItemSettingRowBinding row, @DrawableRes int icon,
                              @StringRes int title, @StringRes int desc) {
        row.rowIcon.setImageResource(icon);
        row.rowTitle.setText(title);
        row.rowDescription.setText(desc);
        row.rowValue.setVisibility(View.GONE);
        row.rowSwitch.setVisibility(View.GONE);
        row.rowColorDot.setVisibility(View.GONE);
        row.rowChevron.setVisibility(View.VISIBLE);
    }

    private void setupColorRow(ItemSettingRowBinding row, @DrawableRes int icon,
                                @StringRes int title, @StringRes int desc, @ColorInt int colorInt) {
        row.rowIcon.setImageResource(icon);
        row.rowTitle.setText(title);
        row.rowDescription.setText(desc);
        row.rowValue.setVisibility(View.GONE);
        row.rowSwitch.setVisibility(View.GONE);
        row.rowChevron.setVisibility(View.GONE);
        row.rowColorDot.setVisibility(View.VISIBLE);
        row.rowColorDot.setBackgroundTintList(ColorStateList.valueOf(colorInt));

    }
}
