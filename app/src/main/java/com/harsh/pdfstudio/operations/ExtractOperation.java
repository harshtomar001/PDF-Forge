package com.harsh.pdfstudio.operations;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.harsh.pdfstudio.fragments.Edit_Pdf_Edit_Page;
import com.harsh.pdfstudio.renderer.ExtractRenderer;

public class ExtractOperation {


    private Bitmap bitmap;
    private Edit_Pdf_Edit_Page.RadioButtonExtract outputType;
    private Context context;
    private String fileName;


    public ExtractOperation(Context context,
                            Bitmap bitmap,
                            Edit_Pdf_Edit_Page.RadioButtonExtract outputType, String fileName){

        this.context=context;
        this.bitmap=bitmap;
        this.outputType=outputType;
        this.fileName=fileName;

    }

    public Uri getFileUri(){

       switch (outputType){

           case PDF:
               return ExtractRenderer.exportBitmapToPdf(context,bitmap,fileName);

           case JPEG:
               return ExtractRenderer.saveBitmapAsJpg(context,bitmap,fileName);

           case PNG:
               return ExtractRenderer.saveBitmapAsPng(context, bitmap,fileName);

           default:
               return null;
       }



    }

}
