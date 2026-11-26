package com.harsh.pdfstudio.model;

public class MergeResult {

    public enum Status {
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final FileItem fileItem;
    private final MergePdfErrorItem error;

    private MergeResult(
            Status status,
            FileItem fileItem,
            MergePdfErrorItem error
    ) {
        this.status = status;
        this.fileItem = fileItem;
        this.error = error;
    }

    public static MergeResult success(FileItem fileItem) {
        return new MergeResult(
                Status.SUCCESS,
                fileItem,
                null
        );
    }

    public static MergeResult error(MergePdfErrorItem error) {
        return new MergeResult(
                Status.ERROR,
                null,
                error
        );
    }

    public Status getStatus() {
        return status;
    }

    public FileItem getFileItem() {
        return fileItem;
    }

    public MergePdfErrorItem getErrorItem() {
        return error;
    }
}
