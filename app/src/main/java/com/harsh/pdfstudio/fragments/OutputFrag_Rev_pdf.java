package com.harsh.pdfstudio.fragments;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.harsh.pdfstudio.R;


public class OutputFrag_Rev_pdf extends BaseOutFragment_rev_rot_pdf {

    public OutputFrag_Rev_pdf(){

        super(R.layout.rotate_pdf_out_frag);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleName.setText(R.string.reverse_pdf);
        titleText.setText(R.string.reverse_your_pdf_pages);
        rot_rev_another.setText(R.string.reverse_another_pdf);
        pdf_rot_rev.setText(R.string.pdf_reversed_successfully);
        pdf_down.setText(R.string.your_pdf_is_reversed_and_ready_to_download);



    }

}