package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class GetFirstPage {

    public static Bitmap getFirstPageBitmap(Context context, Uri pdfUri) {
        try {
            ParcelFileDescriptor fileDescriptor =
                    context.getContentResolver().openFileDescriptor(pdfUri, "r");

            if (fileDescriptor == null) return null;

            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            PdfRenderer.Page page = renderer.openPage(0); // First page

            Bitmap bitmap = Bitmap.createBitmap(
                    page.getWidth(),
                    page.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            page.close();
            renderer.close();
            fileDescriptor.close();

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
