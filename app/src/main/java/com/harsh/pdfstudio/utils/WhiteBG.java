package com.harsh.pdfstudio.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class WhiteBG {


    public static Bitmap addWhiteBackground(Bitmap original) {

        Bitmap result = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(result);

        // Fill the canvas with white
        canvas.drawColor(Color.WHITE);

        // Draw the original bitmap on top
        canvas.drawBitmap(original, 0, 0, null);

        return result;
    }
}
