package com.harsh.pdfstudio.fragments;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.harsh.pdfstudio.controllers.PdfLoaderController;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.ImgAdapter;
import com.harsh.pdfstudio.databinding.CreatePdfPageBinding;
import com.harsh.pdfstudio.model.CreatePdfViewModel;
import java.util.ArrayList;
import java.util.List;


public class CreatePDF_Inp_Frag extends Fragment {

    private CreatePdfPageBinding binding;
    private CreatePdfViewModel viewModel;
    private ImgAdapter imageAdapter;
    private UserClicks clicksListener;
    private PdfLoaderController loaderController;

    public void setOnClickHandler(UserClicks userClicks) {
        this.clicksListener = userClicks;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = CreatePdfPageBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Shared with Activity and every other Fragment
        viewModel = new ViewModelProvider(requireActivity())
                .get(CreatePdfViewModel.class);


        View toolbar = binding.createPdfToolbar.getRoot();

        ViewCompat.setOnApplyWindowInsetsListener(
                toolbar,
                (v, insets) -> {

                    Insets statusBars = insets.getInsets(
                            WindowInsetsCompat.Type.statusBars()
                    );

                    v.setPadding(
                            v.getPaddingLeft(),
                            (int) (statusBars.top / 1.5f),
                            v.getPaddingRight(),
                            v.getPaddingBottom()
                    );

                    return insets;
                }
        );



        //  page stack
        loaderController = new   PdfLoaderController(
                R.animator.loader_page_stack_bounce ,
                binding.page1 ,
                binding.page2 ,
                binding.page3
        );

        initializeImageRecyclerView();
        initializeLoader();
        initializeClickListeners();
        observeViewModel();

    }

    private void initializeImageRecyclerView() {

        binding.recyclerviewForPdf.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        imageAdapter = new ImgAdapter(new ImgAdapter.onClickListeners() {
            @Override
            public void onRemove(Uri uri) {

                viewModel.removeImage(uri);

            }

            @Override
            public void onClick(Uri uri) {

                clicksListener.onItemImageClick(uri);

            }
        });


        binding.recyclerviewForPdf.setAdapter(imageAdapter);
    }

    private void observeViewModel() {

        viewModel.getImageList().observe(getViewLifecycleOwner(), list -> {

            imageAdapter.submitList(new ArrayList<>(list));

            updateUi(list);

        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {

            if(loading){

                loaderController.showLoader();

                binding.loaderLayout.bringToFront();
                binding.loaderLayout.setVisibility(View.VISIBLE);
                binding.loaderLayout.setFocusable(true);
                binding.loaderLayout.setClickable(true);

            }
            else {

                loaderController.hideLoader();


                binding.loaderLayout.setVisibility(View.GONE);
                binding.loaderLayout.setFocusable(false);
                binding.loaderLayout.setClickable(false);

            }

        });

    }

    private void initializeClickListeners() {

        binding.uploadBtn.uploadLayout.setOnClickListener(v -> clicksListener.onUploadClick());

        binding.createPdfToolbar.backArrow.setOnClickListener(v -> clicksListener.onBackClick());

        binding.generatePdf.setOnClickListener(v -> {

            List<Uri> images = viewModel.getImageList().getValue();

            if (images == null || images.isEmpty()) {
                Toast.makeText(requireContext(), "Select Images", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.generatePdf(requireContext().getApplicationContext());
        });

        binding.clearAll.setOnClickListener(v -> viewModel.clearAll());
    }

    private void updateUi(List<Uri> imageList) {

        boolean hasImages = imageList != null && !imageList.isEmpty();

        binding.recyclerviewForPdf.setVisibility(hasImages ? View.VISIBLE : View.GONE);
        binding.emptyImage.setVisibility(hasImages ? View.GONE : View.VISIBLE);
        binding.generatePdf.setEnabled(hasImages);
        binding.generatePdf.setVisibility(hasImages ? View.VISIBLE : View.GONE);
        binding.selectedMeta.setVisibility(hasImages ? View.VISIBLE : View.GONE);
        binding.bottomSection.getRoot().setVisibility(hasImages ? View.VISIBLE : View.GONE);
        binding.selectedImgCount.setText("Selected Images (" + (imageList != null ? imageList.size() : 0) + ")");
    }

    private void initializeLoader() {

        binding.pdfLoader.getSettings().setJavaScriptEnabled(true);
        binding.pdfLoader.setBackgroundColor(Color.TRANSPARENT);
        binding.pdfLoader.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                binding.pdfLoader.evaluateJavascript("setOperation('convert');", null);
            }
        });
        binding.pdfLoader.loadUrl("file:///android_asset/pdf_loader.html");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface UserClicks {
        void onUploadClick();
        void onItemImageClick(Uri imageUri);
        void onBackClick();
    }

}
