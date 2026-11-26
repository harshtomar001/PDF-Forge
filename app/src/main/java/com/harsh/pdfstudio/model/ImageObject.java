package com.harsh.pdfstudio.model;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class ImageObject {

    private Bitmap bitmap;

    private long id;

    private RectF bounds = new RectF();

    private  float aspectRatio;

    private boolean selected=false;

    private float rotation = 0f;

    private boolean flipHorizontal = false;

    private boolean flipVertical = false;

    public void setId(long id){

        this.id = id;

    }

    public  long getId(){
        return  id;
    }


    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public Bitmap getBitmap() {
        return bitmap;
    }


    public void setBounds(float left, float top, float right, float bottom) {
        bounds.set(left, top, right, bottom);
    }


    public RectF getBounds() {
        return bounds;
    }

    public void setAspectRation(float aspectRatio){

        this.aspectRatio=aspectRatio;
    }

    public float getAspectRatio(){

        return  aspectRatio;
    }


    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public boolean isSelected() {
        return selected;
    }


    public float getWidth() {
        return bounds.width();
    }


    public float getHeight() {
        return bounds.height();
    }


    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }


    public float getRotation() {
        return rotation;
    }


    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isFlipHorizontal() {
        return flipHorizontal;
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }

    public boolean isFlipVertical() {
        return flipVertical;
    }

    public void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
    }


}