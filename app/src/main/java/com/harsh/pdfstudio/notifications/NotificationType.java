package com.harsh.pdfstudio.notifications;

/**
 * The category of a notification.
 * Kept as a simple enum so it can be persisted (e.g. as a String/ordinal)
 * in Room, SharedPreferences, or a remote API response later.
 */
public enum NotificationType {
    SUCCESS,
    INFO,
    WARNING
}
