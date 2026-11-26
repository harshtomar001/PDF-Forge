package com.harsh.pdfstudio.model;

import com.harsh.pdfstudio.renderer.MergePdfRenderer;

public class MergePdfErrorItem {

    private final MergePdfItem errorItem;
    private final String message;
    private final MergePdfRenderer.MergeErrorType errorType;

    public MergePdfErrorItem(MergePdfItem item, MergePdfRenderer.MergeErrorType unknown, String message) {
        this.errorItem = item;
        this.errorType =  unknown;
        this.message = message;
    }

    public MergePdfItem getErrorItem() {
        return errorItem;
    }

    public String getMessage() {
        return message;
    }

    public MergePdfRenderer.MergeErrorType getErrorType() {

        return errorType;

    }

}
