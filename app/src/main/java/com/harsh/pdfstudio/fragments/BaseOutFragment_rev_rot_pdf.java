package com.harsh.pdfstudio.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Page_Count;
import com.harsh.pdfstudio.utils.SaveFile;
import com.harsh.pdfstudio.utils.ShareFiles;

public class BaseOutFragment_rev_rot_pdf extends Fragment {


    TextView pdfName;
    TextView pdfSize;
    TextView pdfPages;
    LinearLayout downloadBtn;
    LinearLayout sharePDF;
    LinearLayout backToInpFrag;
    ImageView viewPDF;
    ImageView backArrow;
    Uri pdfUri;
    String name;
    String pages;
    String size;
    TextView titleName;
    TextView titleText;
    TextView pdf_rot_rev;
    TextView pdf_down;
    TextView rot_rev_another;

    FrameLayout toolbar;

    ImageView main_image;
    ImageView pdfIcon;
    ImageView another_task;
    ImageView share;
    ImageView viewPdfIcon;
    TextView shareTextView;
    ImageView shield_image;
    TextView shield_mainText;

    LinearLayout shieldLayout;


    public BaseOutFragment_rev_rot_pdf(int layoutID){

        super(layoutID);

    }



    @Override
    public void onViewCreated(View view , Bundle savedInstanceBundle) {

        super.onViewCreated(view,savedInstanceBundle);

        toolbar=view.findViewById(R.id.rotatePDF_toolbar);
        main_image=view.findViewById(R.id.img);
        pdfIcon=view.findViewById(R.id.pdf_icon);
        another_task=view.findViewById(R.id.rotate_another);
        share=view.findViewById(R.id.share_img);
        viewPdfIcon=view.findViewById(R.id.viewPDF_rot_pdf);
        shareTextView=view.findViewById(R.id.share_textView);
        shield_image=view.findViewById(R.id.shield_image);
        shield_mainText=view.findViewById(R.id.shield_main_text);
        shieldLayout=view.findViewById(R.id.bottom_section_);

        pdfName = view.findViewById(R.id.pdfName_rot_pdf);
        pdfSize = view.findViewById(R.id.pdfSize_rot_pdf);
        pdfPages = view.findViewById(R.id.pdfPages_rot_pdf);
        downloadBtn = view.findViewById(R.id.download_rotate_pdf);
        sharePDF = view.findViewById(R.id.sharePDF_rotatePDF);
        backToInpFrag = view.findViewById(R.id.redirecting_inp_frag);
        viewPDF = view.findViewById(R.id.viewPDF_rot_pdf);
        backArrow = view.findViewById(R.id.back_arrow_rotate_pdf_output);
        titleName=view.findViewById(R.id.output_title);
        titleText=view.findViewById(R.id.output_title_text);
        pdf_rot_rev=view.findViewById(R.id.pdf_rev_rot_success);
        rot_rev_another=view.findViewById(R.id.rot_rev_another);
        pdf_down=view.findViewById(R.id.ready_download);


        backToInpFrag.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager().popBackStack();

        });


        Bundle bundle = getArguments();

        if (bundle != null) {

            name = bundle.getString("pdfName");
            size = bundle.getString("pdfSize");
            pages = bundle.getString("pdfPages");
            pdfUri = bundle.getParcelable("pdf_uri");
            loadData(name, size, pages);
        }



        viewPDF.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), PdfViewerActivity.class);
            intent.putExtra("pdf_uri", pdfUri);
            intent.putExtra("mode","preview");
            startActivity(intent);
        });

        downloadBtn.setOnClickListener(v -> {

            if (pdfUri==null) {
                Toast.makeText(requireContext(),"Null",Toast.LENGTH_SHORT).show();
            }
            Context context = v.getContext();

            EditText input = new EditText(context);
            input.setHint("Enter PDF name");

            new AlertDialog.Builder(context)
                    .setTitle("Save PDF")
                    .setView(input)

                    .setPositiveButton("Save", (dialog, which) -> {

                        String fileName = input.getText().toString().trim();

                        if (fileName.isEmpty()) {
                            fileName = name;
                        }

                        SaveFile.savePDF(requireContext(), pdfUri, fileName, Page_Count.getPdfPageCount(requireContext(), pdfUri), size);
                    })

                    .setNegativeButton("Cancel", null)
                    .show();


        });

        backArrow.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });


        sharePDF.setOnClickListener(v -> {

            Intent intent = ShareFiles.sharePDF();
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            startActivity(Intent.createChooser(intent, "Share PDF"));

        });


}


public void loadData(String name,String size,String pages){

    pdfName.setText(name);
    pdfPages.setText(pages);
    pdfSize.setText(size);

}










}
