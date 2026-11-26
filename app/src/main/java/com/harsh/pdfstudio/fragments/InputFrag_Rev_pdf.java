package com.harsh.pdfstudio.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.RotatedAdapter;
import com.harsh.pdfstudio.utils.PdfToFilterImgUri;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class InputFrag_Rev_pdf extends BaseInpFragment_rev_rot_pdf {

    private onReverse listener;
    public void setOnReverse(onReverse listener){
        this.listener=listener;
    }
    Boolean reverseBtn=false;
    public InputFrag_Rev_pdf(){
        super(R.layout.rotate_pdf_page);

    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);


        page_title.setText(R.string.reverse_pdf);
        toolbar_text_title.setText(R.string.reverse_the_pages_of_your_pdf);
        text_below_upl_pdf.setText(R.string.reverse_your_pdf);
        generate_pdf_icon.setImageResource(R.drawable.transfer_icon_square);
        generate_pdf_text.setText(R.string.reverse_pdf);
        info_text.setText(R.string.info_text_reverse_pdf);
        previewText.setText("Preview (Default PDF)");
        totalPages.setText("Preview (Before Reversing PDF)");
        _0_degree.setText("Default PDF");
        _90_degree.setText("Reverse PDF");
        _0_degree.setTextSize(12);
        _90_degree.setTextSize(12);
        _0_degree.setIcon(null);
        _90_degree.setIcon(null);

        rotatePDF.setOnClickListener(v ->{

            if (list.size()==0){
                Toast.makeText(requireContext(),"Select PDF",Toast.LENGTH_SHORT).show();
                return;
            }

            listener.reversePDF(list.get(0).getUri());
        });


    }

    @Override

    protected void loadData(ArrayList<Integer> indexes){

        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setElevation(400f);

        boolean threeDotAdded=false;


        ArrayList<Integer> index=new ArrayList<>(indexes);

        if (reverseBtn){



            ArrayList<Integer> reverseIndex=new ArrayList<>();
            if (indexes.get(indexes.size()-1)>6) {
                threeDotAdded=true;
                reverseIndex.add(indexes.get(indexes.size() - 1));
                reverseIndex.add(reverseIndex.get(0) - 1);
                reverseIndex.add(reverseIndex.get(0) - 2);
                reverseIndex.add(indexes.get(3) ); // get the middle one // because it remain same on reverse
                reverseIndex.add(indexes.get(3));
                reverseIndex.add(indexes.get(0));
                index=reverseIndex;

            }else{

                Collections.reverse(index);
            }


        }


        ArrayList<Integer> finalIndex = index;
        boolean finaladded=threeDotAdded;

        executor.execute(() -> {

            boolean addedThreeDot=false;


            try {
                prev_imagesUri = PdfToFilterImgUri.filteredImages(requireContext(), list.get(0).getUri(), finalIndex);

                if (finalIndex.size()>=6 && finaladded )   addedThreeDot=true;


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            boolean finalAddedThreeDot = addedThreeDot;


            handler.post(() -> {

                progressBar.setVisibility(View.GONE);

                adapter = new RotatedAdapter(prev_imagesUri,"REVERSE", finalAddedThreeDot,finalIndex);
                adapter.rotation(0f);
                recyclerView.setAdapter(adapter);
            });
        });






    }


    @Override

    protected void _0_degree_Click(){
        setActive(_0_degree, _90_degree);
        reverseBtn=false;
        changePDFPreviewData();

        previewText.setText("Preview (Default PDF)");

    }

    @Override

    protected void _90_degree_Click(){
        setActive(_90_degree, _0_degree);
        reverseBtn=true;
        changePDFPreviewData();

        previewText.setText("Preview (Reverse PDF)");

    }



    public interface onReverse{
        public void reversePDF(Uri uri);
    }






}
