package com.harsh.pdfstudio.notifications;

/**
 * Plain data model for a single notification.
 *
 * This class intentionally has no dependency on Android UI classes so it can
 * later be reused as a Room @Entity, a network DTO, or a ViewModel state
 * object without touching the Activity/Adapter code.
 */
public class NotificationItem {

    private long id;
    private String title;
    private String description;

    /** Raw epoch millis - useful for sorting / grouping by day later. */
    private long timestampMillis;

    /** Pre-formatted, user-facing time string, e.g. "10:45 AM" or "Yesterday". */
    private String displayTime;

    private NotificationType type;
    private boolean isRead;

    public NotificationItem() {
    }

    public NotificationItem(long id,
                             String title,
                             String description,
                             long timestampMillis,
                             String displayTime,
                             NotificationType type,
                             boolean isRead) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timestampMillis = timestampMillis;
        this.displayTime = displayTime;
        this.type = type;
        this.isRead = isRead;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
