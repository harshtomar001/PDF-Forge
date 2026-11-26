package com.harsh.pdfstudio.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.harsh.pdfstudio.creation.GeneratePDF;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.Event;
import com.harsh.pdfstudio.utils.GenerateName;
import com.harsh.pdfstudio.utils.GetFirstPage;
import com.vertlix.utils.file.GetFileName;
import com.vertlix.utils.pdf.Page_Count;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreatePdfViewModel extends ViewModel {

    private final MutableLiveData<List<Uri>> imageList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<FileItem>> pdfResultEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> errorEvent = new MutableLiveData<>();
    private final MutableLiveData<FileItem> lastOutput = new MutableLiveData<>(); // persistent, for open/share/download
    private final MutableLiveData<Bitmap> pdfThumbnail = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LiveData<List<Uri>> getImageList() { return imageList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Event<FileItem>> getPdfResultEvent() { return pdfResultEvent; }
    public LiveData<Event<String>> getErrorEvent() { return errorEvent; }
    public LiveData<FileItem> getLastOutput() { return lastOutput; }
    public LiveData<Bitmap> getPdfThumbnail() { return pdfThumbnail; }

    public void addImages(List<Uri> uris) {

        if (uris == null || uris.isEmpty()) return;

        List<Uri> current = new ArrayList<>(

                imageList.getValue() != null ? imageList.getValue() : new ArrayList<>()

        );
        current.addAll(uris);

        imageList.setValue(current);
    }

    public void removeImage(Uri uri) {

        List<Uri> current = new ArrayList<>(
                imageList.getValue() != null ? imageList.getValue() : new ArrayList<>()
        );
        current.remove(uri);
        imageList.setValue(current);

    }

    public void clearAll() {

        imageList.setValue(new ArrayList<>());
        lastOutput.setValue(null);
        pdfThumbnail.setValue(null);

    }

    public void generatePdf(Context appContext) {

        List<Uri> images = imageList.getValue();

        if (images == null || images.isEmpty()) return;

        String fileName = GenerateName.generateUniqueName("PDF_");

        isLoading.setValue(true);

        executor.execute(() -> {

            try {
                File pdf = GeneratePDF.createPdf(appContext, new ArrayList<>(images), fileName);

                Uri pdfUri = FileProvider.getUriForFile(
                        appContext,
                        appContext.getPackageName() + ".provider",
                        pdf
                );

                String name = GetFileName.getFileName(appContext, pdfUri);
                long size = File_Size.getFileSize(appContext, pdfUri);
                int pages = Page_Count.getPdfPageCount(appContext, pdfUri);

                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                String date = formatter.format(new Date());

                FileItem result = new FileItem(name, pdfUri, size, pages);
                result.setDate(date);

                lastOutput.postValue(result);

                pdfResultEvent.postValue(new Event<>(result));

                generatePdfPreview(appContext, pdfUri);

            } catch (Exception e) {

                errorEvent.postValue(new Event<>(e.getMessage()));


            } finally {

                isLoading.postValue(false);

            }
        });
    }

    public void generatePdfPreview(Context appContext, Uri pdfUri) {

        executor.execute(() -> {

            Bitmap bitmap = GetFirstPage.getFirstPageBitmap(appContext, pdfUri);
            pdfThumbnail.postValue(bitmap);


        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

}