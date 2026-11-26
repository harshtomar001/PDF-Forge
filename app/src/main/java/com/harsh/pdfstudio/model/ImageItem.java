package com.harsh.pdfstudio.model;

import android.net.Uri;

public class ImageItem {

    private  String format;
    private  long size;

    private Uri uri;


    public ImageItem(Uri uri, long size, String format){
        this.uri=uri;
        this.size=size;
        this.format=format;
    }

    public  String getFormat(){
        return format;
    }

    public Uri getUri(){
        return uri;
    }
    public  long getSize(){
        return size;
    }


}
