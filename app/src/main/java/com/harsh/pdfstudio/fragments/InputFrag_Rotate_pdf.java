package com.harsh.pdfstudio.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import java.util.ArrayList;


public class InputFrag_Rotate_pdf extends BaseInpFragment_rev_rot_pdf {


     private onRotatePDF listener;

    public void setOnRotatePDF(onRotatePDF listener){
        this.listener=listener;

    }

    public InputFrag_Rotate_pdf(){
        super(R.layout.rotate_pdf_page);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);


        // on using device's back button to finish the activity
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().finish();
                    }
                }
        );


        rotatePDF.setOnClickListener(v -> {
            if (list.size()==0) {
                Toast.makeText(requireContext(),"Select PDF" ,Toast.LENGTH_SHORT).show();
                return;
            }
            listener.rotate(list);
        });

        previewText.setText(getString(R.string.preview_after_rotation_90,0));


    }

    public interface  onRotatePDF{
         void rotate(ArrayList<FileItem> list);
    }
}