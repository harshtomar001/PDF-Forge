package com.harsh.pdfstudio.useCase;

import android.content.Context;
import android.util.Log;

import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.MergePdfItem;
import com.harsh.pdfstudio.model.MergePdfOptions;
import com.harsh.pdfstudio.model.MergeResult;
import com.harsh.pdfstudio.renderer.MergePdfRenderer;

import java.util.List;

public class MergePdfUseCase {

    private final MergePdfRenderer renderer;

    public MergePdfUseCase(MergePdfRenderer renderer) {
        this.renderer = renderer;
    }

    public MergeResult execute(
            Context context,
            List<MergePdfItem> files,
            MergePdfOptions options
    ) throws Exception {

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException(
                    "No PDF files selected"
            );
        }

        MergeResult result = renderer.render(
                context,
                files,
                options
        );


        return result;

    }
}
