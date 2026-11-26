package com.harsh.pdfstudio.renderer;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class RotationRenderer {

    public static Bitmap rotateBitmap(Bitmap source, int degrees) {

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        return Bitmap.createBitmap(
                source,
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                matrix,
                true
        );
    }


}
