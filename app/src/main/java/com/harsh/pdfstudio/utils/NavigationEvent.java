package com.harsh.pdfstudio.utils;

import com.harsh.pdfstudio.model.FileItem;

public class NavigationEvent {

    public enum Type {
        MERGE_SUCCESS,
        MERGE_FAILED
    }

    private final Type type;
    private final FileItem fileItem;
    private final String errorMessage;

    private NavigationEvent(
            Type type,
            FileItem fileItem,
            String errorMessage
    ) {
        this.type = type;
        this.fileItem = fileItem;
        this.errorMessage = errorMessage;
    }

    public static NavigationEvent mergeSuccess(FileItem fileItem) {
        return new NavigationEvent(
                Type.MERGE_SUCCESS,
                fileItem,
                null
        );
    }

    public static NavigationEvent mergeFailed(String errorMessage) {
        return new NavigationEvent(
                Type.MERGE_FAILED,
                null,
                errorMessage
        );
    }

    public Type getType() {
        return type;
    }

    public FileItem getFileItem() {
        return fileItem;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
