package com.harsh.pdfstudio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.ui.MainActivity;

import org.jspecify.annotations.NonNull;

public class serverDown extends Fragment {

    public serverDown(){
        super(R.layout.activity_server_error);
    }

    @Override
    public void onViewCreated(@NonNull View view , Bundle bundle ){

        super.onViewCreated(view,bundle);

        MaterialButton backToHome=view.findViewById(R.id.btnBackToHome);

        backToHome.setOnClickListener(v -> {

            requireContext().startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();

        });

    }

}
