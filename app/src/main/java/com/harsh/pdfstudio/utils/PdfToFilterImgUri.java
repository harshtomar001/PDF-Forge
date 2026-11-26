package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

public class PdfToFilterImgUri {

    public static  ArrayList<Uri>  filteredImages(Context context, Uri uri,ArrayList<Integer> index) throws IOException {

        ParcelFileDescriptor pfd=context.getContentResolver().openFileDescriptor(uri,"r");
        ArrayList<Uri> imgUri=new ArrayList<>();

        if (pfd!=null){
            PdfRenderer renderer=new PdfRenderer(pfd);
            for(int i:index){

                PdfRenderer.Page page=renderer.openPage(i-1);
                Bitmap bitmap=Bitmap.createBitmap(page.getWidth(),page.getHeight(), Bitmap.Config.ARGB_4444);

                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);

                page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                imgUri.add(SaveBitmapGetUri.getBitmapUri(context,bitmap));
                bitmap.recycle();

            }
            renderer.close();
            pfd.close();
        }
        return imgUri;
    }
}