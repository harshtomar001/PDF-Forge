package com.harsh.pdfstudio.model;

import android.content.Context;

import java.util.List;

public interface PDFRenderer {


    FileItem render(
            Context context,
            List<MergePdfItem> files,
            com.harsh.pdfstudio.model.MergePdfOptions options
    ) throws Exception;
}
