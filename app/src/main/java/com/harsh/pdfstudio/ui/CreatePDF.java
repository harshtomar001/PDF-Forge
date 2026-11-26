package com.harsh.pdfstudio.ui;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.fragments.CreatePDF_Inp_Frag;
import com.harsh.pdfstudio.fragments.CreatePdfErrorFragment;
import com.harsh.pdfstudio.fragments.CreatePdfSuccessFragment;
import com.harsh.pdfstudio.model.CreatePdfViewModel;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.PdfNavigationListener;
import com.harsh.pdfstudio.utils.Image_picker;
import java.util.ArrayList;
import java.util.List;


public class CreatePDF extends AppCompatActivity implements PdfNavigationListener {

    private CreatePdfViewModel viewModel;
    private CreatePDF_Inp_Frag createPDF_inp_frag;
    private CreatePdfSuccessFragment createPdfSuccessFragment;
    private CreatePdfErrorFragment createPdfErrorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_page);

        // Scoped to this Activity — same instance every Fragment will fetch
        viewModel = new ViewModelProvider(this).get(CreatePdfViewModel.class);

        createPDF_inp_frag = new CreatePDF_Inp_Frag();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container, createPDF_inp_frag)
                    .commit();
        }

        setupCreatePdfFragment(createPDF_inp_frag);
        observeViewModel();
    }

    private void observeViewModel() {

        // Fires once per successful generation, navigates to success screen
        viewModel.getPdfResultEvent().observe(this, event -> {

            FileItem result = event.getContentIfNotHandled(); // one-shot wrapper

            if (result == null) return;

            createPdfSuccessFragment = CreatePdfSuccessFragment.newInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container, createPdfSuccessFragment)
                    .commit();

            viewModel.generatePdfPreview(this, result.getUri());

        });

        // Fires once per failed generation
        viewModel.getErrorEvent().observe(this, event -> {

            String message = event.getContentIfNotHandled();
            if (message == null) return;

            createPdfErrorFragment = CreatePdfErrorFragment.newInstance(message);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container, createPdfErrorFragment)
                    .commit();

        });




    }

    @Override
    public void onCreateAnotherPdfClicked() {

        viewModel.clearAll();

        createPDF_inp_frag = new CreatePDF_Inp_Frag();
        setupCreatePdfFragment(createPDF_inp_frag);
        alterFragment(createPDF_inp_frag);

    }

    @Override
    public void onTryAgainPdfClicked() {

        alterFragment(createPDF_inp_frag);

        viewModel.generatePdf(this);


    }




    private ActivityResultLauncher<Intent> pickImagesLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                        Intent data = result.getData();
                        List<Uri> picked = new ArrayList<>();

                        if (data.getClipData() != null) {
                            ClipData clipData = data.getClipData();
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                picked.add(clipData.getItemAt(i).getUri());
                            }
                        } else if (data.getData() != null) {
                            picked.add(data.getData());
                        }

                        // Single source of truth updated directly — no manual sync needed
                        viewModel.addImages(picked);
                    }
            );

    private void alterFragment(Fragment fragment) {

        if (fragment == null) return;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container, fragment)
                .commit();
    }

    private void setupCreatePdfFragment(CreatePDF_Inp_Frag fragment) {

        if (fragment == null) return; // fixed: was checking createPDF_inp_frag, not the param

        fragment.setOnClickHandler(new CreatePDF_Inp_Frag.UserClicks() {

            @Override
            public void onUploadClick() {
                Intent intent = Image_picker.openImageFiles();
                pickImagesLauncher.launch(intent);
            }

            @Override
            public void onItemImageClick(Uri imageUri) {
                Intent intent = new Intent(CreatePDF.this, ImageViewActivity.class);
                intent.putExtra("image_uri", imageUri);
                startActivity(intent);
            }

            @Override
            public void onBackClick() {
                finish();
            }


        });
    }




}