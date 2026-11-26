package com.harsh.pdfstudio.creation;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.FileProvider;
import androidx.loader.content.Loader;


//import com.harsh.pdfstudio.CustomXmlParserFactory;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfPage;
//import com.itextpdf.kernel.pdf.PdfReader;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.kernel.utils.XmlProcessorCreator;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RotatingPDF {

    public static Uri rotatePDF(
            Context context,
            Uri uri
    ) throws IOException {

        File inputFile = copyUriToCache(context, uri);

        File outputFile = new File(
                context.getCacheDir(),
                "rotated_" + System.currentTimeMillis() + ".pdf"
        );

        try (PDDocument document = PDDocument.load(inputFile)) {

            for (PDPage page : document.getPages()) {

                int rotation = page.getRotation();

                page.setRotation((rotation + 90) % 360);
            }

            document.save(outputFile);
        }

        inputFile.delete();

        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                outputFile
        );
    }

    public static File copyUriToCache(Context context, Uri uri) throws IOException {

        String fileName = "input_" + System.currentTimeMillis() + ".pdf";

        File outputFile = new File(
                context.getCacheDir(),
                fileName
        );

        try (
                InputStream inputStream =
                        context.getContentResolver()
                                .openInputStream(uri);

                FileOutputStream outputStream =
                        new FileOutputStream(outputFile)
        ) {

            if (inputStream == null) {
                throw new IOException("Unable to open input URI");
            }

            byte[] buffer = new byte[64 * 1024];

            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        }

        return outputFile;
    }

}
