package com.harsh.pdfstudio.creation;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.FileProvider;
//import com.harsh.pdfstudio.CustomXmlParserFactory;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfReader;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.kernel.utils.PdfMerger;
//import com.itextpdf.kernel.utils.XmlProcessorCreator;
import com.harsh.pdfstudio.utils.FileCacheUtils;
import com.harsh.pdfstudio.utils.GenerateName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReversingPDF {

    private static final String TAG = "ReversingPDF";

    public static Uri reversePDF(
            Context context,
            Uri uri
    ) {

        File inputFile = null;
        File outputFile = null;

        try {


            inputFile =
                    FileCacheUtils.copyUriToCache(
                            context,
                            uri
                    );


            outputFile = new File(
                    context.getCacheDir(),
                    GenerateName.generateUniqueName("rev_")
                    );



            try (
                    PDDocument sourceDocument =
                            PDDocument.load(inputFile);

                    PDDocument outputDocument =
                            new PDDocument()
            ) {

                int totalPages =
                        sourceDocument.getNumberOfPages();

                for (
                        int i = totalPages - 1;
                        i >= 0;
                        i--
                ) {

                    PDPage sourcePage =
                            sourceDocument.getPage(i);

                    PDPage importedPage =
                            outputDocument.importPage(
                                    sourcePage
                            );

                    importedPage.setRotation(
                            sourcePage.getRotation()
                    );
                }



                outputDocument.save(
                        outputFile
                );
            }


            if (
                    !outputFile.exists()
                            || outputFile.length() == 0
            ) {

                Log.e(
                        TAG,
                        "Output PDF is empty"
                );

                return null;
            }



            if (inputFile.exists()) {
                inputFile.delete();
            }


            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName()
                            + ".provider",
                    outputFile
            );


        } catch (Exception e) {

            Log.e(
                    TAG,
                    "PDF reversal failed",
                    e
            );


            // Cleanup input file

            if (
                    inputFile != null
                            && inputFile.exists()
            ) {

                inputFile.delete();
            }


            // Cleanup incomplete output

            if (
                    outputFile != null
                            && outputFile.exists()
                            && outputFile.length() == 0
            ) {

                outputFile.delete();
            }


            return null;
        }
    }
}