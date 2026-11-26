package com.harsh.pdfstudio.creation;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.loader.content.Loader;

import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.utils.GenerateName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.vertlix.utils.file.File_Size;
import com.vertlix.utils.file.GetFileName;
import com.vertlix.utils.pdf.Page_Count;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Spliting_PDF {


    public static Uri getSplitPDF(Context context,
                                  Uri pdfUri,
                                  int fromPage,
                                  int toPage ){


        try {

            InputStream stream=context.getContentResolver().openInputStream(pdfUri);

        PDDocument sourceDocument = PDDocument.load(stream);

        PDDocument outputDocument = new PDDocument();

        for (int i = fromPage - 1; i < toPage; i++) {

            PDPage page = sourceDocument.getPage(i);

            outputDocument.importPage(page);
        }

        File outputFile=new File(context.getCacheDir(),
                GenerateName.generateUniqueName(String.format("pg_%d_%d_splt_",fromPage,toPage)));

            outputDocument.save(outputFile);

            Uri outputUri= FileProvider.getUriForFile(context, context.getPackageName()+".provider",outputFile);

            sourceDocument.close();
            outputDocument.close();

            return  outputUri;




        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public static ArrayList<FileItem> getIndividualPDFs(Context context,Uri pdfUri){

        ArrayList<FileItem> list=new ArrayList<>();

        try{
            InputStream stream=context.getContentResolver().openInputStream(pdfUri);
            PDDocument sourcePDF=PDDocument.load(stream);

            int totalPages=sourcePDF.getNumberOfPages();

            for (int i=0;i<totalPages;i++){
                PDDocument outputPDF=new PDDocument();

                PDPage page=sourcePDF.getPage(i);
                outputPDF.importPage(page);

                String fileName= GenerateName.generateUniqueName(String.format("pg_%d_splt_",i+1)); // along with .pdf extension

                File outputFile=new File(context.getCacheDir(),fileName);

                outputPDF.save(outputFile);

                Uri uri=FileProvider.getUriForFile(context,context.getPackageName()+".provider",outputFile);

                FileItem item=new FileItem(
                        GetFileName.getFileName(context,uri),
                        uri,
                        File_Size.getFileSize(context,uri),
                        Page_Count.getPdfPageCount(context,uri));

                list.add(item);
                outputPDF.close();

            }

            sourcePDF.close();
            return list;


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



}
