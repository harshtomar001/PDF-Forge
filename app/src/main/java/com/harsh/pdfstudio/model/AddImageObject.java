package com.harsh.pdfstudio.model;

import android.graphics.RectF;
import android.net.Uri;

public class AddImageObject {

    private Uri  imageUri;

    private RectF bounds;

    private float rotation;

    private boolean isFlipVertical;

    private boolean isFlipHorizontal;

    private  float opacity;

    private  int zIndex;

    private  int pageIndex;

    public AddImageObject(int pageIndex,
                          Uri imageUri,
                          RectF bounds ,
                          float rotation,
                          boolean isFlipHorizontal ,
                          boolean isFlipVertical){


            this.pageIndex = pageIndex;

            this.imageUri = imageUri;

            this.bounds = bounds;

            this.isFlipVertical = isFlipVertical;

            this.isFlipHorizontal = isFlipHorizontal;

            this.rotation = rotation;


        }


    public RectF getBounds() {
        return bounds;
    }

    public boolean isFlipHorizontal() {
        return isFlipHorizontal;
    }

    public boolean isFlipVertical() {
        return isFlipVertical;
    }


    public Uri getImageUri() {
        return imageUri;
    }


    public float getRotation(){

        return rotation;

    }

    public int getPageIndex() {
        return pageIndex;
    }

    public float getOpacity() {
        return opacity;
    }

    public int getzIndex() {
        return zIndex;
    }
}
