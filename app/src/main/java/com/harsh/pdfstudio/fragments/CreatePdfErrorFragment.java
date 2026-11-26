package com.harsh.pdfstudio.fragments;

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

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.databinding.CreatePdfErrorBinding;
import com.harsh.pdfstudio.model.CreatePdfViewModel;
import com.harsh.pdfstudio.model.PdfNavigationListener;

public class CreatePdfErrorFragment extends Fragment {

    private CreatePdfErrorBinding binding;
    private CreatePdfViewModel viewModel;

    public static CreatePdfErrorFragment newInstance(String message) {

        return new CreatePdfErrorFragment();

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = CreatePdfErrorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

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

        // Same ViewModel instance used by the Activity
        // and other Create PDF fragments.
        viewModel = new ViewModelProvider(requireActivity())
                .get(CreatePdfViewModel.class);

        observeViewModel();
        setClickListeners();
    }

    private void observeViewModel() {

        viewModel.getErrorEvent().observe(
                getViewLifecycleOwner(),
                message -> {

                    if (binding == null) {
                        return;
                    }

                    if (message != null && !message.isEmpty()) {

                        binding.errorMessage.setText(message.peekContent());

                    }
                    else {

                        binding.errorMessage.setText(
                                getString(R.string.error_message_default)
                        );

                    }
                }
        );
    }

    private void setClickListeners() {

        // Back arrow
        binding.backArrow.setOnClickListener(v -> {

           requireActivity().finish();

        });

        // Try again
        binding.tryAgain.setOnClickListener(v -> {

            if (getActivity() instanceof PdfNavigationListener) {
                ((PdfNavigationListener) getActivity())
                        .onTryAgainPdfClicked();
            }
        });

        // Create another PDF
        binding.createAnotherPdf.setOnClickListener(v -> {

            viewModel.clearAll();

            if (getActivity() instanceof PdfNavigationListener) {
                ((PdfNavigationListener) getActivity())
                        .onCreateAnotherPdfClicked();
            }
        });

        // Back button
        binding.back.setOnClickListener(v -> {

           requireActivity().finish();

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}