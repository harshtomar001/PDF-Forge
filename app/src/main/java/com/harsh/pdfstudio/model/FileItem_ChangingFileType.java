package com.harsh.pdfstudio.model;

import android.net.Uri;

public class FileItem_ChangingFileType {
    private String name;
    private Uri uri;

    private  int PageCount;
    private  long FileSize;

    private  String extension;
    public  FileItem_ChangingFileType(String name, Uri uri, long size, int pageCount, String extension){
        this.name=name;
        this.uri=uri;
        this.FileSize=size;
        this.PageCount=pageCount;
        this.extension=extension;
    }

    public long getFileSize() {
        return FileSize;
    }

    public int getPageCount() {
        return PageCount;
    }

    public String getName() {
        return name;
    }

    public Uri getUri() {
        return uri;
    }

    public String getExtension() {
        return extension;
    }

}
