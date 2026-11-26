package com.harsh.pdfstudio.model;

import android.graphics.Bitmap;

import com.harsh.pdfstudio.fragments.Edit_Pdf_Edit_Page;

public class WatermarkConfig {
    private boolean isImageWatermark;
    private String text;
    private Bitmap imageBitmap;
    private float opacity;
    private int size;
    private int rotation;
    private Edit_Pdf_Edit_Page.WatermarkPosition position;


    public WatermarkConfig(boolean isImageWatermark,
                           String text,
                           Bitmap imageBitmap,
                           float opacity,
                           int size,
                           int rotation,
                           Edit_Pdf_Edit_Page.WatermarkPosition position
    ){
        this.isImageWatermark=isImageWatermark;
        this.imageBitmap=imageBitmap;
        this.text=text;
        this.opacity=opacity;
        this.size=size;
        this.rotation=rotation;
        this.position=position;

    }


    public boolean getIsImageWatermark(){
        return isImageWatermark;
    }
    public  Bitmap getImageBitmap(){
        return imageBitmap;
    }

    public String getText(){
        return text;
    }

    public float getOpacity(){
        return opacity;
    }

    public int getSize(){
        return size;
    }

    public int getRotation(){
        return rotation;
    }

    public Edit_Pdf_Edit_Page.WatermarkPosition getPosition(){
        return position;
    }


}
