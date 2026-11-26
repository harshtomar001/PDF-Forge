package com.harsh.pdfstudio.model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.harsh.pdfstudio.renderer.MergePdfRenderer;
import com.harsh.pdfstudio.ui.MergePdfActivity;
import com.harsh.pdfstudio.ui.MergePdfActivity.MergePdfState;
import com.harsh.pdfstudio.ui.MergePdfActivity.PageMargin;
import com.harsh.pdfstudio.ui.MergePdfActivity.PageSizeMode;
import com.harsh.pdfstudio.useCase.MergePdfUseCase;
import com.harsh.pdfstudio.utils.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MergePdfViewModel extends ViewModel {

    private final MutableLiveData<List<MergePdfItem>> pdfs = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<MergePdfState> state = new MutableLiveData<>(MergePdfState.IDLE);

    private final MutableLiveData<PageSizeMode> pageSizeMode = new MutableLiveData<>(PageSizeMode.ORIGINAL);

    private final MutableLiveData<PageMargin> pageMargin = new MutableLiveData<>(PageMargin.NONE);

    private final MutableLiveData<MergePdfOptions> mergePdfOptions =
            new MutableLiveData<>(
                    new MergePdfOptions(
                            PageSizeMode.ORIGINAL ,
                            PageMargin.NONE)
            );

    private final MutableLiveData<Event<FileItem>> result = new MutableLiveData<>();

    private final MutableLiveData<Event<MergePdfErrorItem>> error = new MutableLiveData<>();


    private final MutableLiveData<FileItem> mergeResult = new MutableLiveData<>();

    public LiveData<FileItem> getMergeResult() {

        return mergeResult;

    }

    private final MutableLiveData<MergePdfErrorItem> mergeError = new MutableLiveData<>();

    public LiveData<MergePdfErrorItem> getMergeError() {

        return mergeError;

    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MergePdfUseCase mergePdfUseCase;


    public MergePdfViewModel(
            MergePdfUseCase mergePdfUseCase
    ) {
        this.mergePdfUseCase = mergePdfUseCase;
    }


    public LiveData<List<MergePdfItem>> getPdfs() {
        return pdfs;
    }

    public LiveData<MergePdfState> getState() {
        return state;
    }


    public LiveData<Event<FileItem>> getResultEvent() {

        return result;

    }

    public LiveData<Event<MergePdfErrorItem>> getErrorEvent() {

        return error;

    }

    public LiveData<MergePdfOptions> getMergePdfOptions(){

        return mergePdfOptions;

    }

    public void setPageSizeMode(PageSizeMode pageSizeMode){

        this.pageSizeMode.setValue(pageSizeMode);

        mergePdfOptions.setValue(
                new MergePdfOptions(
                        this.pageSizeMode.getValue() ,
                        pageMargin.getValue()
                )
        );

    }

    public void setPageMargin(PageMargin pageMargin){

        this.pageMargin.setValue(pageMargin);

        mergePdfOptions.setValue(new MergePdfOptions(pageSizeMode.getValue() , pageMargin));

    }


    public void addPdf(MergePdfItem item) {

        List<MergePdfItem> current = pdfs.getValue();

        ArrayList<MergePdfItem> updated =
                current == null
                        ? new ArrayList<>()
                        : new ArrayList<>(current);

        updated.add(item);

        pdfs.setValue(updated);
    }


    public void removePdf(MergePdfItem item) {

        List<MergePdfItem> current = pdfs.getValue();

        if (current == null) {
            return;
        }

        ArrayList<MergePdfItem> updated =
                new ArrayList<>(current);

        updated.removeIf(
                pdf -> pdf.getId().equals(item.getId())
        );

        pdfs.setValue(updated);
    }

    public void clearPdfs() {

        List<MergePdfItem> list = pdfs.getValue();

        list.clear();

        pdfs.setValue(list);

    }


    public void swapPDFS( int fromPosition, int toPosition ) {

        List<MergePdfItem> current =
                pdfs.getValue();

        if (current == null) {
            return;
        }

        if (fromPosition < 0 ||
                toPosition < 0 ||
                fromPosition >= current.size() ||
                toPosition >= current.size()) {
            return;
        }

        ArrayList<MergePdfItem> updated =
                new ArrayList<>(current);

        MergePdfItem item =
                updated.remove(fromPosition);

        updated.add(toPosition, item);

        pdfs.setValue(updated);
    }


    public void merge(Context context, MergePdfOptions options) {

        List<MergePdfItem> current = pdfs.getValue();

        if (current == null || current.isEmpty()) {

            error.postValue(

                    new Event<>(
                            new MergePdfErrorItem(
                                    null,
                                    MergePdfRenderer.MergeErrorType.UNKNOWN,
                                    "Please select at least one PDF"
                            ))
            );


            state.setValue(
                    MergePdfState.ERROR
            );

            return;
        }

        state.setValue(
                MergePdfState.MERGING
        );

        executor.execute(() -> {

            try {

                MergeResult output = mergePdfUseCase.execute(
                                context,
                                new ArrayList<>(current),
                                options
                );


                if (output.getStatus() == MergeResult.Status.ERROR) {


                    MergePdfErrorItem  mergePdfErrorItem = output.getErrorItem();


                    error.postValue(new Event<>(
                            mergePdfErrorItem
                    ));

                    state.postValue(
                            MergePdfState.ERROR
                    );

                    mergeError.postValue(mergePdfErrorItem);

                    return;
                }

                result.postValue( new Event<>( output.getFileItem()) );

                mergeResult.postValue(output.getFileItem());

                state.postValue( MergePdfActivity.MergePdfState.SUCCESS );

            }
            catch (Exception e) {


                MergePdfErrorItem mergePdfItemError = new MergePdfErrorItem(

                        null,
                        MergePdfRenderer.MergeErrorType.UNKNOWN,
                        "Unable to merge PDFs"

                );

                error.postValue( new Event<>(mergePdfItemError) );

                state.postValue( MergePdfState.ERROR );

                mergeError.postValue(mergePdfItemError);

            }

        });
    }


    public void clearAllData(){

        pdfs.setValue(new ArrayList<>());

        state.setValue(MergePdfState.IDLE);

        setPageSizeMode(PageSizeMode.ORIGINAL);
        setPageMargin(PageMargin.NONE);

        mergeResult.setValue(null);


    }


    @Override
    protected void onCleared() {

        super.onCleared();

        executor.shutdownNow();
    }

}