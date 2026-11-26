package com.harsh.pdfstudio.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;


//  for split pdf
public class SharingViewModel_sp_pdf extends ViewModel {
    private final MutableLiveData<ArrayList<FileItem>> fileList = new MutableLiveData<>();

    public void setFileList(ArrayList<FileItem> files) {
        fileList.setValue(files);
    }

    public LiveData<ArrayList<FileItem>> getFileList() {
        return fileList;
    }

    public void removeFile(int position) {

        ArrayList<FileItem> current = fileList.getValue();

        if (current == null || position < 0 || position >= current.size())
            return;

        current.remove(position);

        fileList.setValue(current);
    }

}
