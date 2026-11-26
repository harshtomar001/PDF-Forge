package com.harsh.pdfstudio.model;

import android.net.Uri;

public class PageItem {

    public enum Source {
        PDF,
        IMAGE
    }
    private Source source;
    private int pdfPageIndex;
    private long id;
    private Uri imageUri;
    private  boolean isSelected=false;

    private boolean isDirty=false;
    //  to be used in the  main page whenever there is more op applied to the  page // it sets to true so that we cannot take the stale bitmap from the  lrucache

    public PageItem(int pdfPageIndex) {
        source = Source.PDF;
        this.pdfPageIndex = pdfPageIndex;
        this.id=System.nanoTime();
    }

    public PageItem(Uri imageUri,int imageIndex) {
        source = Source.IMAGE;
        this.imageUri = imageUri;
        this.pdfPageIndex=imageIndex;
        this.id=System.nanoTime();

    }


    public Source getSource() {
        return source;
    }

    public int getPdfPageIndex() {
        return pdfPageIndex;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public int getImageFakeIndex(){
        return pdfPageIndex;
    }

    public void setImageUri(Uri uri){

        this.imageUri = uri;

    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected=isSelected;
    }

    public  boolean getIsSelected(){
        return  isSelected;
    }

    public long getId(){
        return  this.id;
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    public boolean isDirty() {
        return isDirty;
    }
}