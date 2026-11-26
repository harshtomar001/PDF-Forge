package com.harsh.pdfstudio.model;

import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

public class AddTextObject {

    // Page
    private int pageIndex;

    // Content
    private String text;


    // Position in PDF coordinates
    private RectF bounds;

    private boolean positioned = false;

    // Transform
    private float rotation;

    // Appearance
    private float textSize;
    private int textColor;
    private float opacity = 1f;

    // Font

    private String fontFamily = null;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikeThrough;

    private int fontWeight = 400;

    private boolean fontFallbackWarningShown = false;
    private boolean weightFallbackWarningShown = false;


    // Alignment
    private Paint.Align textAlign = Paint.Align.LEFT;


    private boolean isHint;


    // ID
    private long id;

    public AddTextObject(String text,
                         float textSize,
                         int textColor,
                         float opacity
                         ) {

        id = System.nanoTime();
        this.text = text;
        this.textSize = textSize;
        this.textColor = textColor;
        this.opacity = opacity;



    }



    public void setText(String text){
        this.text = text;

    }

    public void  setTextSize(float size){
        this.textSize = size;
    }

    public float getTextSize(){

        return  textSize;

    }

    public void  setTextColor(int color){

        this.textColor = color;

    }

    public int getTextColor(){

        return  textColor;

    }

    public  void  setOpacity(float opacity){
        this.opacity = opacity;
    }

    public  float getOpacity(){
        return  opacity;
    }

    public  void setFontFamily(String  fontFamily){

        this.fontFamily = fontFamily;

    }

    public  String getFontFamily(){

        return fontFamily;

    }

    public void setFontWeight(int weight){

        this.fontWeight = weight;

    }

    public int getFontWeight(){

        return  this.fontWeight;

    }

    public int getPageIndex() {
        return pageIndex;
    }


    public RectF getBounds() {

        return bounds;

    }


    public void setBounds(RectF bounds) {

        this.bounds = bounds;

    }


    public String getText() {
        return text;
    }

    public Paint.Align getTextAlign() {
        return textAlign;
    }


    public void  setBold(boolean bold){

        this.bold = bold;

    }


    public boolean isBold() {
        return bold;
    }


    public boolean isPositioned() {
        return positioned;
    }

    public void setPositioned(boolean positioned) {
        this.positioned = positioned;
    }


    public boolean isHint(){
        return isHint;
    }

    public void setHint(boolean isHint){
        this.isHint = isHint;
    }



    public boolean isItalic() {
        return italic;
    }

    public void  setUnderline(boolean underline){
        this.underline = underline;
    }
    public boolean isStrikeThrough() {
        return strikeThrough;
    }

    public void  setStrikeThrough(boolean strikeThrough){

        this.strikeThrough = strikeThrough;

    }

    public void  setItalic(boolean italic){

        this.italic = italic;

    }

    public void  setRotation(float rotation){

        this.rotation =rotation;

    }


    public float getRotation() {
        return rotation;
    }


    public boolean isUnderline() {
        return underline;
    }


    public long getId() {
        return id;
    }


    public boolean isFontFallbackWarningShown() {
        return fontFallbackWarningShown;
    }

    public void setFontFallbackWarningShown(boolean shown) {
        this.fontFallbackWarningShown = shown;
    }

    public boolean isWeightFallbackWarningShown() {
        return weightFallbackWarningShown;
    }

    public void setWeightFallbackWarningShown(boolean shown) {
        this.weightFallbackWarningShown = shown;
    }


}