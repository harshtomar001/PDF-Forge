package com.harsh.pdfstudio.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.harsh.pdfstudio.R;

public class Pptx_Pdf_Out_Frag extends BaseOutFragment_rev_rot_pdf {



    public Pptx_Pdf_Out_Frag() {
           super(R.layout.rotate_pdf_out_frag);
        }

    @Override
    public void onViewCreated(@NonNull View view, Bundle saved) {

        super.onViewCreated(view, saved);

        toolbar.setBackgroundResource(R.drawable.ppt_pdf_toolbar_bg);
        main_image.setImageResource(R.drawable.ppt_orange);
        pdfIcon.setImageResource(R.drawable.pdf_icon_orange);
        ViewGroup.LayoutParams params = pdfIcon.getLayoutParams();
        params.width = 150;
        params.height = 150;
        pdfIcon.setLayoutParams(params);
        another_task.setImageResource(R.drawable.rotate_orange);
        share.setImageResource(R.drawable.share_icon_orange);
        viewPdfIcon.setImageResource(R.drawable.eye_icon_orange);
        downloadBtn.setBackgroundResource(R.drawable.down_pdf_ppt_pdf_bg);
        sharePDF.setBackgroundResource(R.drawable.share_outline_ppt_pdf);
        shareTextView.setTextColor(ContextCompat.getColor(requireContext(),R.color.light_orange));
        rot_rev_another.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
        shield_image.setImageResource(R.drawable.shield_check_icon_orange);

        shield_mainText.setTextColor(ContextCompat.getColor(requireContext(),R.color.orange));
        shieldLayout.setBackgroundResource(R.drawable.shield_bg_ppt_pdf);

        titleName.setText("Converted PDF");
        titleText.setVisibility(View.GONE);
        rot_rev_another.setText("Convert Another PPT to PDF");
        pdf_rot_rev.setText("PPT to PDF Converted Successfully");
        pdf_down.setText("Your PPT file is converted and Ready to Download");



    }
}