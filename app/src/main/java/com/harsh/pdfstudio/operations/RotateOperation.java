package com.harsh.pdfstudio.operations;

import android.graphics.Bitmap;
import com.harsh.pdfstudio.renderer.RotationRenderer;

public class RotateOperation implements PageOperation {

    private final int degrees;


    public RotateOperation(int degrees){
        this.degrees=degrees;
    }


    @Override
    public void apply(PageRenderContext renderContext){
        Bitmap bitmap= RotationRenderer.rotateBitmap(renderContext.getBitmap(),degrees);

        renderContext.replaceBitmap(bitmap);

    }



}
