package com.harsh.pdfstudio.operations;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class PageRenderContext {

    private Bitmap bitmap;
    private Canvas canvas;

    public PageRenderContext(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.canvas = new Canvas(bitmap);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Canvas getCanvas() {
        return canvas;
    }


    public void replaceBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.canvas = new Canvas(bitmap);
    }
}