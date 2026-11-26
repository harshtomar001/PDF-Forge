package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.fragments.MergePdfError;
import com.harsh.pdfstudio.fragments.MergePdfInputFragment;
import com.harsh.pdfstudio.fragments.MergePdfResult;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.MergePdfErrorItem;
import com.harsh.pdfstudio.model.MergePdfItem;
import com.harsh.pdfstudio.model.MergePdfOptions;
import com.harsh.pdfstudio.model.MergePdfViewModel;
import com.harsh.pdfstudio.model.MergePdfViewModelFactory;
import com.harsh.pdfstudio.renderer.MergePdfRenderer;
import com.harsh.pdfstudio.useCase.MergePdfUseCase;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;

import java.util.ArrayList;


public class MergePdfActivity extends AppCompatActivity
        implements MergePdfInputFragment.UserClicks {

    MergePdfViewModel viewModel ;


    public enum PageSizeMode {

        ORIGINAL,

        FIT_SMALLEST_WIDTH,
        FIT_SMALLEST_HEIGHT,
        FIT_SMALLEST_BOTH,

        FIT_LARGEST_WIDTH,
        FIT_LARGEST_HEIGHT,
        FIT_LARGEST_BOTH,

        A4,
        A3,
        LEGAL,
        LETTER
    }

    public enum PageMargin {

        NONE,
        SMALL,
        MEDIUM,
        LARGE
    }

    public enum MergePdfState {
        IDLE,
        MERGING,
        SUCCESS,
        ERROR
    }


    private static final String TAG_MERGE_FRAGMENT = "merge_pdf_input_fragment";

    private MergePdfInputFragment mergePdfInputFragment;


    private final ActivityResultLauncher<Intent> pdfPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }


                Intent data = result.getData();

                if (data.getClipData() != null) {
                    // Multiple files selected.
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {

                        MergePdfItem item = createItemFromUri(data.getClipData().getItemAt(i).getUri());

                        if (viewModel == null) return;

                        viewModel.addPdf(item);

                    }
                }
                else if (data.getData() != null) {

                    MergePdfItem item = createItemFromUri(data.getData());

                    if (viewModel == null) return;

                    viewModel.addPdf(item);
                }


            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_pdf); // container with a fragment host id

        FragmentManager fragmentManager = getSupportFragmentManager();

        MergePdfRenderer renderer = new MergePdfRenderer();

        MergePdfUseCase useCase = new MergePdfUseCase(renderer);

        MergePdfViewModelFactory factory = new MergePdfViewModelFactory(useCase);

        viewModel = new ViewModelProvider(
                this,
                factory
        ).get(MergePdfViewModel.class);


        mergePdfInputFragment = (MergePdfInputFragment)
                fragmentManager.findFragmentByTag(TAG_MERGE_FRAGMENT);

        if (mergePdfInputFragment == null) {

            mergePdfInputFragment = new MergePdfInputFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.mergeFragmentContainer, mergePdfInputFragment, TAG_MERGE_FRAGMENT);
            transaction.commit();

        }

        if (viewModel != null){

            viewModel.getResultEvent().observe(this , event -> {

                if (event ==  null) return;

                FileItem  item =  event.getContentIfNotHandled();

                if (item == null) return;

                showMergeResult();

            });


            viewModel.getErrorEvent().observe(this , stringEvent -> {

                if (stringEvent ==  null) return;

                MergePdfErrorItem mergePdfErrorItem = stringEvent.getContentIfNotHandled();


                if (mergePdfErrorItem == null) return;

                showMergeError();

            });


        }

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        finish();
                    }
                }
        );

    }


    @Override
    public void onAddPdfClick() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPickerLauncher.launch(intent);

    }

    @Override
    public void onBackClick() {

        finish();

    }

    private void showMergeResult() {

        MergePdfResult fragment = MergePdfResult.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mergeFragmentContainer,
                        fragment
                )
                .addToBackStack(null)
                .commit();
    }

    private void showMergeError(){

        MergePdfError  mergePdfError = new MergePdfError();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mergeFragmentContainer,
                        mergePdfError
                )
                .addToBackStack(null)
                .commit();



    }

    public void startNewMerge(){


        getSupportFragmentManager().clearBackStack(null);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.mergeFragmentContainer,
                        new MergePdfInputFragment()
                )
                .commit();
    }



    private MergePdfItem createItemFromUri(Uri uri) {

        String name = "Document.pdf";
        long size = 0L;

        try {

            name = GetFileName.getFileName(this , uri);
            size = File_Size.getFileSize(this , uri);

        }
        catch (Exception ignored) {
            // Fall back to the defaults above if the provider can't resolve metadata.
        }

        return new MergePdfItem(uri, name, size);

    }


}
