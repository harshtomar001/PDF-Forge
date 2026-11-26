package com.harsh.pdfstudio.model;

import android.net.Uri;

public class FileItem {

    private String name;
    private Uri uri;

    private  int PageCount;
    private  long FileSize;

    private  String date = null;

    public FileItem(String name, Uri uri,long size,int pageCount) {
        this.name = name;
        this.uri = uri;
        this.PageCount=pageCount;
        this.FileSize=size;
    }

    public String getName() {
        return name;
    }

    public Uri getUri() {
        return uri;
    }

    public int getPageCount() {
        return PageCount;
    }

    public long getFileSize() {
        return FileSize;
    }

    public void setDate(String date){
        this.date = date;

    }

    public String getDate() {
        return date;
    }
}
