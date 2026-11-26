package com.harsh.pdfstudio.creation;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.utils.Page_Count;
import com.tom_roush.pdfbox.io.MemoryUsageSetting;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class PDF_Merging {

    public static FileItem mergePDFs(Context context, ArrayList<FileItem> pdfUris) {

        if (pdfUris == null || pdfUris.isEmpty()) {
            return null;
        }

        ArrayList<InputStream> inputStreams = new ArrayList<>();

        try {
            File outputDir = new File(context.getExternalFilesDir(null), "PDFStudio");

            if (!outputDir.exists()) outputDir.mkdirs();

            File outputFile = new File(outputDir, "mrg_" + System.currentTimeMillis() + ".pdf");

            PDFMergerUtility merger = new PDFMergerUtility();

            merger.setDestinationStream(new FileOutputStream(outputFile));

            boolean hasMerged = false;

            for (FileItem item : pdfUris) {
                Uri uri = item.getUri();

                try {
                    InputStream is = context.getContentResolver().openInputStream(uri);

                    if (is == null) {
                        Log.e("PDF_MERGE", "InputStream null: " + uri);
                        continue;
                    }

                    merger.addSource(is);
                    inputStreams.add(is);
                    hasMerged = true;

                } catch (Exception e) {
                    Log.e("PDF_MERGE", "Error reading PDF: " + uri, e);
                }
            }

            if (!hasMerged) {
                Log.e("PDF_MERGE", "No valid PDF found");
                closeStreams(inputStreams);
                return null;
            }

            merger.mergeDocuments(
                    MemoryUsageSetting.setupTempFileOnly()
            );

            closeStreams(inputStreams);

            if (!outputFile.exists() || outputFile.length() == 0) {
                Log.e("PDF_MERGE", "Merge failed or empty file");
                return null;
            }

            Uri outputUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    outputFile
            );

            String filename = outputFile.getName();
            int lastIndex = filename.lastIndexOf('.');
            String fileName = filename.substring(0, lastIndex);

            long fileSize = outputFile.length();
            int pageCount = Page_Count.getPdfPageCount(context, outputUri);

            return new FileItem(fileName, outputUri, fileSize, pageCount);

        } catch (Exception e) {
            Log.e("PDF_MERGE", "Fatal error", e);
        } finally {
            closeStreams(inputStreams);
        }

        return null;
    }

    private static void closeStreams(ArrayList<InputStream> inputStreams) {
        for (InputStream is : inputStreams) {
            try {
                if (is != null) is.close();
            } catch (Exception ignored) {
            }
        }
    }
}
