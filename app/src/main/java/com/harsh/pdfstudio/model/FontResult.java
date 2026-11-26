package com.harsh.pdfstudio.model;

import android.graphics.Typeface;

public class FontResult {

    private Typeface typeface;

    public  boolean fontFallback;
    public  boolean weightFallback;

    public FontResult(Typeface typeface ,
                      boolean weightFallback ,
                      boolean fontFallback
    ){

        this.weightFallback = weightFallback;
        this.fontFallback = fontFallback;
        this.typeface = typeface;

    }

    public Typeface getTypeface(){

        return typeface;

    }


}
