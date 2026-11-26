package com.harsh.pdfstudio.renderer;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class CropRenderer {


    public static Bitmap getCroppedBitmap(Bitmap bitmap, Rect cropRect){

        return Bitmap.createBitmap(bitmap,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height());



    }


}
