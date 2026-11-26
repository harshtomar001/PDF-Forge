package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class PdfToImageUri {

    public static ArrayList<Uri> convertPdfPagesToImageUris(Context context, Uri pdfUri) {
        ArrayList<Uri> imageUris = new ArrayList<>();

        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(pdfUri, "r");
            if (pfd == null) return imageUris;

            PdfRenderer pdfRenderer = new PdfRenderer(pfd);
            int pageCount = pdfRenderer.getPageCount();

            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = pdfRenderer.openPage(i);

                int width = page.getWidth() * 2;
                int height = page.getHeight() * 2;

                Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);


                Matrix matrix = new Matrix();
                matrix.setScale(2f, 2f);


                page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();

                // Save bitmap as temporary image

                File tempFile = new File(context.getCacheDir(), "pdf_page_"+System.currentTimeMillis() + (i+1) + ".png");
                FileOutputStream fos = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();

                // Get URI for the image
                Uri imageUri = FileProvider.getUriForFile(context,

                       context.getPackageName()+".provider",tempFile

                        );

                imageUris.add(imageUri);
            }

            pdfRenderer.close();
            pfd.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageUris;
    }
}