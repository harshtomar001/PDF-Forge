package com.harsh.pdfstudio.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.harsh.pdfstudio.operations.PageOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SharingPages_VM_edt_pdf extends ViewModel {

    private final MutableLiveData<Set<Long>> modifiedPagesSet=new MutableLiveData<>();
    private final MutableLiveData<HashMap<Long, List<PageOperation>>> operations_hashmap =new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Set<Long>> selectedIDs=new MutableLiveData<>(new HashSet<>());

    // sharing data to  edit pdf page from the adapter
    private final MutableLiveData<List<PageItem>> selectedPageItems=new MutableLiveData<>(new ArrayList<>());

    public void addSelectedPageItem(PageItem item){

        Objects.requireNonNull(selectedPageItems.getValue()).add(item);
        selectedIDs.getValue().add(item.getId());

    }

    public void removeSelectedPageItem(PageItem item){

        Objects.requireNonNull(selectedPageItems.getValue()).remove(item);
        selectedIDs.getValue().remove(item.getId());

    }

    public LiveData<List<PageItem>> getSelectedPageItems(){
        return  selectedPageItems;

    }

    public void clearSelectedPageItems(){
        selectedPageItems.setValue(new ArrayList<>());
        selectedIDs.setValue(new HashSet<>());
    }

    public LiveData<Set<Long>> getSelectedIDs(){
        return selectedIDs;
    }

    public void  setModifiedPagesMap(HashMap<Long, List<PageOperation>>  hashMap){

        operations_hashmap.setValue(hashMap);

        for (Map.Entry<Long, List<PageOperation>> entry : hashMap.entrySet()) {

            Long key = entry.getKey();

            modifiedPagesSet.getValue().add(key);

        }

    }

    public void addPageToModifiedMap(Long key, List<PageOperation> value){

        operations_hashmap.getValue().put(key,value);
        modifiedPagesSet.getValue().add(key);
    }

    public void removePageFromModifiedMap(Long key){
        Objects.requireNonNull(operations_hashmap.getValue()).remove(key);

        Objects.requireNonNull(modifiedPagesSet.getValue()).remove(key);
    }

    public List<PageOperation> getPageOperationList(Long key){
        return operations_hashmap.getValue().get(key);
    }

    public LiveData<Set<Long>> getModifiedIdSet(){
        return  modifiedPagesSet;
    }

    public HashMap<Long, List<PageOperation>> getModifiedPageOperationsMap(){
        return  operations_hashmap.getValue();
    }


}
