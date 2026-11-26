package com.harsh.pdfstudio.model;

import android.net.Uri;
import java.util.Locale;
import java.util.UUID;


public class MergePdfItem {

    private final String id;
    private final Uri uri;
    private final String name;
    private final long size;

    public MergePdfItem(Uri uri, String name, long size) {
        // Stable id independent of list position, used as the DiffUtil/stable id
        // and to correlate ItemTouchHelper drag events.
        this.id = UUID.randomUUID().toString();
        this.uri = uri;
        this.name = name;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }


    public String getFormattedSize() {

        if (size <= 0) {
            return "0 KB";
        }
        double kb = size / 1024.0;
        double mb = kb / 1024.0;
        if (mb >= 1) {
            return String.format(Locale.getDefault(), "%.2f MB", mb);
        }
        return String.format(Locale.getDefault(), "%.0f KB", kb);
    }
}
