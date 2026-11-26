package com.harsh.pdfstudio.operations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import com.harsh.pdfstudio.renderer.WatermarkRenderer;
import com.harsh.pdfstudio.fragments.Edit_Pdf_Edit_Page;
import java.io.IOException;

public class WatermarkOperation implements PageOperation {

    private final Context context;
    private final Uri iconImage;
    private final boolean isImageWatermark;
    private final String text;
    private final Edit_Pdf_Edit_Page.WatermarkPosition position;
    private final int rotation;
    private final float size;
    private final float opacity;

    public WatermarkOperation(Context context,
                              Uri iconImage,
                              boolean isImageWatermark,
                              String text,
                              Edit_Pdf_Edit_Page.WatermarkPosition position,
                              float size,
                              int rotation,
                              float opacity) {

        this.context = context;
        this.iconImage = iconImage;
        this.isImageWatermark = isImageWatermark;
        this.text = text;
        this.position = position;
        this.rotation = rotation;
        this.size =  size;
        this.opacity = opacity;
    }

    public boolean isImageWatermark() {
        return isImageWatermark;
    }

    public Uri getWatermarkImageUri() {
        return iconImage;
    }

    public String getWatermarkText() {
        return text;
    }

    public Edit_Pdf_Edit_Page.WatermarkPosition getCurrentPosition() {
        return position;
    }


    public int getRotation() {
        return rotation;
    }

    public float getOpacity() {
        return opacity;
    }

    public float getSize() {
        return size;
    }


    public Context getContext() {
        return context;
    }

    @Override
    public void apply(PageRenderContext renderContext) throws IOException {


        Bitmap pageBitmap = renderContext.getBitmap();

        Canvas canvas = renderContext.getCanvas();

        WatermarkRenderer.applyWatermark(pageBitmap,canvas,this);



    }


}
