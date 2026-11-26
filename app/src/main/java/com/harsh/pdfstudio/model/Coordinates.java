package com.harsh.pdfstudio.model;

public class Coordinates {

    private float x_coor;
    private float y_coor;
    private  float watermarkWidth=0f;
    private  float watermarkHeight=0f;

    public Coordinates(float x_coor,float y_coor){

        this.x_coor=x_coor;
        this.y_coor=y_coor;

    }
    public float getX_coor() {
        return x_coor;
    }


    public float getY_coor() {
        return y_coor;
    }

    public float getWatermarkHeight() {
            return watermarkHeight;
    }

    public float getWatermarkWidth() {
        return watermarkWidth;
    }
    public  void  setWatermarkWidth(float watermarkWidth){
        this.watermarkWidth=watermarkWidth;
    }

    public void setWatermarkHeight(float watermarkHeight) {
        this.watermarkHeight = watermarkHeight;
    }
}
