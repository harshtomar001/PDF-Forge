package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.util.Pair;
import com.harsh.pdfstudio.model.PageItem;
import java.io.IOException;
import java.io.InputStream;

public class GenerateBitmapFromPDF {

    private final Object pdfLock = new Object();

    public  Bitmap renderPage(int pageIndex,PdfRenderer pdfRenderer) {

        synchronized (pdfLock) {

            PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);

            float scale = 0.2f;

            int width = (int)(page.getWidth() * scale);
            int height = (int)(page.getHeight() * scale);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();

            return bitmap;
        }
    }

    public Bitmap renderHighQualPage(int pageIndex,PdfRenderer pdfRenderer,int width){

        synchronized (pdfLock) {

            PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);

            float scale = (float) width / page.getWidth();

            int height = (int) (page.getHeight() * scale);


            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Matrix matrix = new Matrix();

            matrix.setScale(scale, scale);

            page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            page.close();

            return bitmap;
        }


    }

    public  float getScaleFactor(PageItem item, PdfRenderer pdfRenderer, Context context){

        if(item.getSource()== PageItem.Source.IMAGE){

            Uri imageUri = item.getImageUri();

            if (context == null) throw new IllegalArgumentException("Context cannot be null");

            if (imageUri == null) throw new IllegalArgumentException("Image Uri cannot be null");


            Pair<Float, Float> pair=getImageSize(context , imageUri);

            if((pair.first == 0) || (pair.second == 0)) return -1f;

            return  pair.first.floatValue() / pair.second.floatValue() ;

        }
        else if(item.getSource() == PageItem.Source.PDF){

            PdfRenderer.Page page = pdfRenderer.openPage(item.getPdfPageIndex());

            float width = page.getWidth();
            float height = page.getHeight();

            if(width == 0 || height == 0 ) return  -1f;

            return width / height ;

        }


        return -1f;
    }

    private static Pair<Float, Float> getImageSize(Context context, Uri imageUri) {

        try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(is, null, options);

            float width = options.outWidth;
            float height = options.outHeight;

            return new Pair<>(width,height);




        }
        catch (IOException e) {

            e.printStackTrace();

        }

        return new Pair<>(0f, 0f);
    }

}