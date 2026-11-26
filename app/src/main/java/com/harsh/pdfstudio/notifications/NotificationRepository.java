package com.harsh.pdfstudio.notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Single source of truth for notification data as far as the UI is concerned.
 *
 * Today this returns an in-memory sample list. Later you can change the body
 * of {@link #getNotifications()} to:
 *   - query a Room DAO,
 *   - read/write a SharedPreferences-backed cache,
 *   - call a repository/ViewModel that hits an API,
 * without touching NotificationActivity or NotificationAdapter at all, since
 * they only depend on List<NotificationItem>.
 */
public class NotificationRepository {

    public static List<NotificationItem> getNotifications() {
        List<NotificationItem> list = new ArrayList<>();

        list.add(new NotificationItem(
                1,
                "PDF Merged Successfully",
                "Your files have been merged into mrg_20250828_094512.pdf",
                System.currentTimeMillis(),
                "10:45 AM",
                NotificationType.SUCCESS,
                false
        ));

        list.add(new NotificationItem(
                2,
                "App Update Available",
                "A new version of PDF Studio is available (v2.1.0). Tap to update now.",
                System.currentTimeMillis() - 86_400_000L,
                "Yesterday",
                NotificationType.INFO,
                false
        ));

        list.add(new NotificationItem(
                3,
                "PDF Split Completed",
                "The document has been successfully split into 5 pages.",
                System.currentTimeMillis() - 90_000_000L,
                "Yesterday",
                NotificationType.SUCCESS,
                true
        ));

        list.add(new NotificationItem(
                4,
                "PDF Compressed Successfully",
                "Your file has been compressed and saved as compressed_file.pdf",
                System.currentTimeMillis() - 172_800_000L,
                "Apr 26, 2025",
                NotificationType.SUCCESS,
                true
        ));

        list.add(new NotificationItem(
                5,
                "Storage Space Low",
                "Your app storage is almost full. Consider clearing some files.",
                System.currentTimeMillis() - 259_200_000L,
                "Apr 25, 2025",
                NotificationType.WARNING,
                true
        ));

        list.add(new NotificationItem(
                6,
                "PDF Converted Successfully",
                "Your file has been converted to PDF and saved.",
                System.currentTimeMillis() - 345_600_000L,
                "Apr 24, 2025",
                NotificationType.SUCCESS,
                true
        ));

        return list;
    }
}
