package com.harsh.pdfstudio.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.harsh.pdfstudio.utils.GenerateBitmapFromImgUri;

import java.io.IOException;

public class ReplaceRenderer {

    public static Bitmap replaceBitmap(Context context, Uri imageUri) throws IOException {

        Bitmap imageBitmap= GenerateBitmapFromImgUri.generateBitmapFromUri(context,imageUri,0.8f);

        return imageBitmap;

    }

}
