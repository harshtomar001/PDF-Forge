package com.harsh.pdfstudio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.harsh.pdfstudio.databinding.CreatePdfSuccessBinding;
import com.harsh.pdfstudio.model.CreatePdfViewModel;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.PdfNavigationListener;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.SaveFile;
import com.harsh.pdfstudio.utils.ShareFiles;


public class CreatePdfSuccessFragment extends Fragment {

    private CreatePdfSuccessBinding binding;
    private CreatePdfViewModel viewModel;

    // No more newInstance() / Bundle args needed — everything comes from the ViewModel
    public static CreatePdfSuccessFragment newInstance() {
        return new CreatePdfSuccessFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = CreatePdfSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        if (binding.toolbarHomePage!= null) {

            ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarHomePage,

                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(WindowInsetsCompat.Type.statusBars());

                        v.setPadding(
                                systemBars.left,
                                (int) (systemBars.top / 2f ) ,
                                v.getPaddingRight(),
                                systemBars.bottom
                        );

                        return insets;
                    });
        }

        // Scoped to the Activity — same instance the Activity and other Fragments use
        viewModel = new ViewModelProvider(requireActivity()).get(CreatePdfViewModel.class);

        observeViewModel();
        setClickListeners();
    }

    private void observeViewModel() {

        // Persistent state — safe to re-observe after rotation, always shows current output
        viewModel.getLastOutput().observe(getViewLifecycleOwner(), result -> {
            if (result == null || binding == null) return;

            binding.pdfName.setText(result.getName());
            binding.pdfPages.setText(String.valueOf(result.getPageCount()));
            binding.pdfSize.setText(Byte_MB.convertByte(result.getFileSize()));
            binding.pdfDate.setText(result.getDate());
        });

        // Thumbnail arrives async; observer fires whenever it's ready,
        // regardless of whether the view existed yet when generation finished
        viewModel.getPdfThumbnail().observe(getViewLifecycleOwner(), bitmap -> {
            if (bitmap == null || binding == null) return;

            Glide.with(requireContext())
                    .load(bitmap)
                    .thumbnail(0.4f)
                    .into(binding.pdfPreview);
        });
    }

    private void setClickListeners() {

        binding.backArrow.setOnClickListener(v -> requireActivity().finish());

        binding.openPdf.setOnClickListener(v -> {

            FileItem output = viewModel.getLastOutput().getValue();
            if (output == null || output.getUri() == null) return;

            Intent intent = new Intent(requireContext(), PdfViewerActivity.class);
            intent.putExtra("pdf_uri", output.getUri());
            intent.putExtra("mode", "preview");
            startActivity(intent);

        });

        binding.sharePdf.setOnClickListener(v -> {

            FileItem output = viewModel.getLastOutput().getValue();
            if (output == null || output.getUri() == null) return;

            Intent intent = ShareFiles.sharePDF();
            intent.putExtra(Intent.EXTRA_STREAM, output.getUri());
            startActivity(Intent.createChooser(intent, "Share PDF"));

        });

        binding.downloadPdf.setOnClickListener(v -> {

            FileItem output = viewModel.getLastOutput().getValue();
            if (output == null) return;

            SaveFile.savePDF(requireContext(),
                    output.getUri(),
                    output.getName(),
                    output.getPageCount(),
                    Byte_MB.convertByte(output.getFileSize()),
                    true
            );

        });

        binding.createAnotherPdf.setOnClickListener(v -> {

            viewModel.clearAll();
            // Ask the hosting Activity to swap fragments — see note below
            if (getActivity() instanceof PdfNavigationListener) {

                ((PdfNavigationListener) getActivity()).onCreateAnotherPdfClicked();

            }

        });

        binding.done.setOnClickListener(v -> requireActivity().finish());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
