package com.harsh.pdfstudio.managers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Single source of truth for every PDF Studio setting.
 * <p>
 * SettingsFragment (and anywhere else in the app, e.g. AppStorageManager,
 * the notification dispatcher, or the PDF export pipeline) should read and
 * write settings only through this class -- never touch SharedPreferences
 * directly. That keeps persistence, key names and default values in one
 * place and makes the values easy to unit test.
 */
public class SettingsManager {

    private static final String PREFS_NAME = "pdf_studio_settings";

    // ---- Preference keys -------------------------------------------------
    private static final String KEY_THEME = "key_theme";
    private static final String KEY_ACCENT_COLOR = "key_accent_color";
    private static final String KEY_DEFAULT_PAGE_SIZE = "key_default_page_size";
    private static final String KEY_DEFAULT_MARGIN = "key_default_margin";
    private static final String KEY_REMEMBER_LAST_SETTINGS = "key_remember_last_settings";
    private static final String KEY_AUTO_CLEANUP_ENABLED = "key_auto_cleanup_enabled";
    private static final String KEY_CLEANUP_DURATION = "key_cleanup_duration";
    private static final String KEY_PDF_COMPLETION_NOTIF = "key_pdf_completion_notif";
    private static final String KEY_ERROR_NOTIF = "key_error_notif";
    private static final String KEY_NOTIFICATION_SOUND = "key_notification_sound";
    private static final String KEY_OPEN_PDF_AFTER_CREATION = "key_open_pdf_after_creation";
    private static final String KEY_SAVE_GENERATED_PDFS = "key_save_generated_pdfs";
    private static final String KEY_ASK_BEFORE_OVERWRITE = "key_ask_before_overwrite";

    // ---- Enums -------------------------------------------------------------

    public enum Theme { SYSTEM_DEFAULT, LIGHT, DARK }

    public enum AccentColor { PURPLE, BLUE, GREEN, ORANGE }

    public enum PageSize { ORIGINAL, A4, A3, LETTER, LEGAL }

    public enum PageMargin { NONE, SMALL, MEDIUM, LARGE }

    /** Values are stored as the duration in milliseconds. */
    public enum CleanupDuration {
        MIN_30(30L * 60_000L),
        HOUR_1(60L * 60_000L),
        HOUR_2(2L * 60 * 60_000L),
        HOUR_6(6L * 60 * 60_000L),
        HOUR_24(24L * 60 * 60_000L);

        public final long millis;

        CleanupDuration(long millis) {
            this.millis = millis;
        }
    }

    // ---- Defaults ------------------------------------------------------

    private static final Theme DEFAULT_THEME = Theme.SYSTEM_DEFAULT;
    private static final AccentColor DEFAULT_ACCENT_COLOR = AccentColor.PURPLE;
    private static final PageSize DEFAULT_PAGE_SIZE = PageSize.A4;
    private static final PageMargin DEFAULT_MARGIN = PageMargin.NONE;
    private static final boolean DEFAULT_REMEMBER_LAST_SETTINGS = true;
    private static final boolean DEFAULT_AUTO_CLEANUP_ENABLED = true;
    private static final CleanupDuration DEFAULT_CLEANUP_DURATION = CleanupDuration.HOUR_2;
    private static final boolean DEFAULT_PDF_COMPLETION_NOTIF = true;
    private static final boolean DEFAULT_ERROR_NOTIF = true;
    private static final boolean DEFAULT_NOTIFICATION_SOUND = true;
    private static final boolean DEFAULT_OPEN_PDF_AFTER_CREATION = false;
    private static final boolean DEFAULT_SAVE_GENERATED_PDFS = true;
    private static final boolean DEFAULT_ASK_BEFORE_OVERWRITE = true;

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ---- Appearance ------------------------------------------------------

    public Theme getTheme() {
        return Theme.valueOf(prefs.getString(KEY_THEME, DEFAULT_THEME.name()));
    }

    public void setTheme(Theme theme) {
        prefs.edit().putString(KEY_THEME, theme.name()).apply();
    }

    public AccentColor getAccentColor() {
        return AccentColor.valueOf(prefs.getString(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR.name()));
    }

    public void setAccentColor(AccentColor accentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, accentColor.name()).apply();
    }

    // ---- PDF settings ----------------------------------------------------

    public PageSize getDefaultPageSize() {
        return PageSize.valueOf(prefs.getString(KEY_DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE.name()));
    }

    public void setDefaultPageSize(PageSize pageSize) {
        prefs.edit().putString(KEY_DEFAULT_PAGE_SIZE, pageSize.name()).apply();
    }

    public PageMargin getDefaultMargin() {
        return PageMargin.valueOf(prefs.getString(KEY_DEFAULT_MARGIN, DEFAULT_MARGIN.name()));
    }

    public void setDefaultMargin(PageMargin margin) {
        prefs.edit().putString(KEY_DEFAULT_MARGIN, margin.name()).apply();
    }

    public boolean isRememberLastSettingsEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_LAST_SETTINGS, DEFAULT_REMEMBER_LAST_SETTINGS);
    }

    public void setRememberLastSettingsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMEMBER_LAST_SETTINGS, enabled).apply();
    }

    // ---- Storage -----------------------------------------------------------

    public boolean isAutomaticCleanupEnabled() {
        return prefs.getBoolean(KEY_AUTO_CLEANUP_ENABLED, DEFAULT_AUTO_CLEANUP_ENABLED);
    }

    public void setAutomaticCleanupEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_CLEANUP_ENABLED, enabled).apply();
    }

    public CleanupDuration getCleanupDuration() {
        return CleanupDuration.valueOf(prefs.getString(KEY_CLEANUP_DURATION, DEFAULT_CLEANUP_DURATION.name()));
    }

    public void setCleanupDuration(CleanupDuration duration) {
        prefs.edit().putString(KEY_CLEANUP_DURATION, duration.name()).apply();
    }

    /**
     * Convenience accessor for AppStorageManager.AUTO_CLEANUP_MAX_AGE_MILLIS --
     * wire that constant to this value (or read it directly here) so cleanup
     * always respects what the user picked in Settings.
     */
    public long getCleanupDurationMillis() {
        return getCleanupDuration().millis;
    }

    // ---- Notifications -----------------------------------------------------

    public boolean isPdfCompletionNotificationEnabled() {
        return prefs.getBoolean(KEY_PDF_COMPLETION_NOTIF, DEFAULT_PDF_COMPLETION_NOTIF);
    }

    public void setPdfCompletionNotificationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PDF_COMPLETION_NOTIF, enabled).apply();
    }

    public boolean isErrorNotificationEnabled() {
        return prefs.getBoolean(KEY_ERROR_NOTIF, DEFAULT_ERROR_NOTIF);
    }

    public void setErrorNotificationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ERROR_NOTIF, enabled).apply();
    }

    public boolean isNotificationSoundEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATION_SOUND, DEFAULT_NOTIFICATION_SOUND);
    }

    public void setNotificationSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND, enabled).apply();
    }

    // ---- PDF operations ------------------------------------------------

    public boolean isOpenPdfAfterCreationEnabled() {
        return prefs.getBoolean(KEY_OPEN_PDF_AFTER_CREATION, DEFAULT_OPEN_PDF_AFTER_CREATION);
    }

    public void setOpenPdfAfterCreationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_OPEN_PDF_AFTER_CREATION, enabled).apply();
    }

    public boolean isSaveGeneratedPdfsEnabled() {
        return prefs.getBoolean(KEY_SAVE_GENERATED_PDFS, DEFAULT_SAVE_GENERATED_PDFS);
    }

    public void setSaveGeneratedPdfsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SAVE_GENERATED_PDFS, enabled).apply();
    }

    public boolean isAskBeforeOverwriteEnabled() {
        return prefs.getBoolean(KEY_ASK_BEFORE_OVERWRITE, DEFAULT_ASK_BEFORE_OVERWRITE);
    }

    public void setAskBeforeOverwriteEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ASK_BEFORE_OVERWRITE, enabled).apply();
    }

    // ---- Reset -----------------------------------------------------------

    /** Restores every setting handled by this class to its default value. */
    public void resetToDefaults() {
        prefs.edit().clear().apply();
    }
}
