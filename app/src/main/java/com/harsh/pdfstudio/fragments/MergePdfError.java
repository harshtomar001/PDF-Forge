package com.harsh.pdfstudio.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import com.harsh.pdfstudio.databinding.FragmentMergePdfErrorBinding;
import com.harsh.pdfstudio.model.MergePdfErrorItem;
import com.harsh.pdfstudio.model.MergePdfViewModel;
import com.harsh.pdfstudio.renderer.MergePdfRenderer;
import com.harsh.pdfstudio.ui.MergePdfActivity;

public class MergePdfError extends Fragment {

    private FragmentMergePdfErrorBinding binding;

    private MergePdfViewModel viewModel;



    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentMergePdfErrorBinding
                .inflate(inflater,
                        container,
                        false
                );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (binding.toolbar!= null) {

            ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar,
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


        handleClickListeners();

        viewModel =   new ViewModelProvider(requireActivity())
                .get(MergePdfViewModel.class);


        if (viewModel != null){

            viewModel.getMergeError().observe(
                    getViewLifecycleOwner(),
                    mergePdfErrorItem -> {

                        if (mergePdfErrorItem == null || binding == null) {
                            return;
                        }

                        handleMergeError(mergePdfErrorItem);
                    }
            );


        }



    }

    private void handleClickListeners(){

        if (binding ==  null) return;

        binding.btnBack.setOnClickListener(v -> {

            getFragmentManager().popBackStack();

        });

        binding.btnClose.setOnClickListener(v -> {

            requireActivity().finish();

        });

        binding.btnChooseDifferentFiles.setOnClickListener(v -> {

            if (viewModel != null){
                viewModel.clearAllData();

                MergePdfActivity activity =
                        (MergePdfActivity) requireActivity();

                activity.startNewMerge();

            }

        });

        binding.btnTryAgain.setOnClickListener(v -> {


            MergePdfActivity activity =
                    (MergePdfActivity) requireActivity();

            activity.startNewMerge();

            if (viewModel ==  null) return;

            viewModel.merge(requireContext() , viewModel.getMergePdfOptions().getValue());


        });
    }

    private void handleMergeError(MergePdfErrorItem error) {

        binding.tvErrorMessage.setText(error.getMessage());

        MergePdfRenderer.MergeErrorType type = error.getErrorType();



        switch (type) {

            case INPUT_FILE_READ_ERROR:

                showInputFileReadError(error);
                break;

            case CORRUPTED_PDF:

                showCorruptedPdfError(error);
                break;

            case OUTPUT_DIRECTORY_ERROR:

                showOutputDirectoryError(error);
                break;

            case OUTPUT_CREATION_ERROR:

                showOutputCreationError(error);
                break;

            case PASSWORD_PROTECTED:

                showProtectedPdfError(error);
                break;

            case UNKNOWN:

            default:

                showUnknownError(error);
                break;
        }
    }

    private void showInputFileReadError(MergePdfErrorItem error) {

        binding.tvErrorTitle.setText("Unable to read PDF");

        binding.tvErrorMessage.setText(
                "PDF Studio could not read this file."
        );


        binding.tvProblemFileName.setVisibility(View.GONE);

        if (error.getErrorItem().getName() != null) {
            binding.tvProblemFileName.setVisibility(View.VISIBLE);
            binding.tvProblemFileName.setText(error.getErrorItem().getName());
        }
    }

    private void showCorruptedPdfError(MergePdfErrorItem error) {

        Log.d("corrupted pdf ", error.getMessage());

        binding.tvErrorTitle.setText("Corrupted PDF");

        binding.tvErrorMessage.setText(
                "This PDF appears to be corrupted or cannot be processed."
        );

        binding.tvProblemFileName.setVisibility(View.GONE);

        if (error.getErrorItem().getName() != null) {

            binding.tvProblemFileName.setVisibility(View.VISIBLE);
            binding.tvProblemFileName.setText(error.getErrorItem().getName());

        }
    }

    private void showProtectedPdfError(MergePdfErrorItem error) {

        Log.d(
                "protected_pdf",
                error.getMessage()
        );

        binding.tvErrorTitle.setText(
                "Password Protected PDF"
        );

        binding.tvErrorMessage.setText(
                "This PDF is password protected and cannot be processed without the password."
        );

        binding.tvProblemFileName.setVisibility(
                View.GONE
        );

        if (error.getErrorItem().getName() != null) {

            binding.tvProblemFileName.setVisibility(
                    View.VISIBLE
            );

            binding.tvProblemFileName.setText(
                    error.getErrorItem().getName()
            );
        }
    }

    private void showOutputDirectoryError(MergePdfErrorItem error) {

        binding.tvErrorTitle.setText(
                "Storage error"
        );

        binding.tvErrorMessage.setText(
                "PDF Studio could not prepare storage for the merged PDF."
        );

        binding.tvProblemFileName.setVisibility(View.GONE);
    }

    private void showOutputCreationError(MergePdfErrorItem error) {

        binding.tvErrorTitle.setText(
                "Merge failed"
        );

        binding.tvErrorMessage.setText(
                "The merged PDF could not be created."
        );

        binding.tvProblemFileName.setVisibility(View.GONE);
    }

    private void showUnknownError(MergePdfErrorItem error) {

        binding.tvErrorTitle.setText(
                "Something went wrong"
        );

        binding.tvErrorMessage.setText(
                "PDF Studio couldn't complete this operation."
        );

        binding.tvProblemFileName.setVisibility(View.GONE);
    }

}
