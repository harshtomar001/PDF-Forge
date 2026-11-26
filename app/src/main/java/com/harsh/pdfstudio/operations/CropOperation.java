package com.harsh.pdfstudio.operations;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.harsh.pdfstudio.renderer.CropRenderer;

public class CropOperation implements PageOperation {

    private Rect coordinates;

    public CropOperation(Rect coordinates) {
        this.coordinates = coordinates;
    }

    public Rect getCoordinates() {
        return coordinates;
    }

    @Override

    public void apply(PageRenderContext renderContext){

        Bitmap bitmap= CropRenderer.getCroppedBitmap(renderContext.getBitmap(),coordinates);

        renderContext.replaceBitmap(bitmap);

    }


}
