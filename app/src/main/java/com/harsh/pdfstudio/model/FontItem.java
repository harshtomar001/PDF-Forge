package com.harsh.pdfstudio.model;

import android.graphics.Typeface;

public class FontItem {

    private final String fontName;
    private final Typeface typeface;

    public FontItem(String fontName, Typeface typeface) {
        this.fontName = fontName;
        this.typeface = typeface;
    }

    public String getFontName() {
        return fontName;
    }

    public Typeface getTypeface() {
        return typeface;
    }
}