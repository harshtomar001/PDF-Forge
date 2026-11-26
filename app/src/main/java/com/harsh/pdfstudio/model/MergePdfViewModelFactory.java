package com.harsh.pdfstudio.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.harsh.pdfstudio.useCase.MergePdfUseCase;

public class MergePdfViewModelFactory implements ViewModelProvider.Factory {

    private final MergePdfUseCase mergePdfUseCase;

    public MergePdfViewModelFactory(
            MergePdfUseCase mergePdfUseCase
    ) {
        this.mergePdfUseCase = mergePdfUseCase;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {

        if (modelClass.isAssignableFrom(
                MergePdfViewModel.class
        )) {

            return (T) new MergePdfViewModel(
                    mergePdfUseCase
            );
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class"
        );
    }
}