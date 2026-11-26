package com.harsh.pdfstudio.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class Sharing_data_adap_edt_pdf extends ViewModel {

    private MutableLiveData<List<PageItem>> list=new MutableLiveData<>();


    public void setFileList(List<PageItem> map){
        list.setValue(map);
    }

    public LiveData<List<PageItem>> getFileList(){

        return  list;

    }


}
