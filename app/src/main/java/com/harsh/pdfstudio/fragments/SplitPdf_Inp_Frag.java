package com.harsh.pdfstudio.fragments;

import static android.app.Activity.RESULT_OK;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.RangeSlider;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.Split_PDF_Adap_1;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.PDF_Picker;
import com.vertlix.utils.pdf.Page_Count;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SplitPdf_Inp_Frag extends Fragment {

    ImageView backArrow;
    ActivityResultLauncher<Intent> pdfLauncher;

    ArrayList<Integer> pdfPages=new ArrayList<>();
    ArrayList<Integer> previewPages=new ArrayList<>();

    ///

    LinearLayout extractPagesRanges;
    LinearLayout individualPagesCard;
    ImageView extractPagesRangesIcon;
    TextView extractPagesRangesText;
    ImageView individualPagesIcon;
    TextView individualPagesCardText;

    ///
    MaterialCardView pdf_preview;
    TextView pdfName;
    TextView pdfMetaData;
    ImageView remove_pdf;
    LinearLayout uploadPdf;
    ///

    TextView selectSplitMethod;
    LinearLayout splitCard;
    TextView pagePreview;
    MaterialButton splitPDFBtn;
    ///

    TextView selectMorePage;
    LinearLayout pageRangeLayout;
    LinearLayout quickLayout;
    private  int keyBoardHeight;
    FrameLayout floatingframeLayout;
    EditText floatingEditText;
    EditText fromPageEditBox;
    EditText toPageEditBox;
    ImageView no_pdf_selected;
    RangeSlider rangeSlider;
    TextView rangeText;
    TextView pageCountTxt;

    Split_PDF_Adap_1 adapter;

    RecyclerView pdf_page_preview_recyclerView;

    int[] activeTextBox=new int[]{-1};

    List<FileItem> list=new ArrayList<>();

    private onSplitPDF listener;

    int[] btnSelected_for_SplitPDF=new int[]{0};



    public void onSplitPDFClick(onSplitPDF listener){
        this.listener=listener;
    }


    public  SplitPdf_Inp_Frag(){

        super(R.layout.split_pdf_inp_frag);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedBundle){

        super.onViewCreated(view,savedBundle);


        backArrow=view.findViewById(R.id.back_arrow_split_pdf);
        ///
        extractPagesRanges=view.findViewById(R.id.extractRangeCard);
        individualPagesCard=view.findViewById(R.id.individualPagesCard);
        extractPagesRangesIcon=view.findViewById(R.id.extractRange_icon);
        extractPagesRangesText=view.findViewById(R.id.extract_range_text);
        individualPagesCardText=view.findViewById(R.id.individualPagesCardText);
        individualPagesIcon=view.findViewById(R.id.individualPagesCardIcon);
        ///

        pdf_preview=view.findViewById(R.id.pdf_preview_card_split_pdf);
        remove_pdf=view.findViewById(R.id.remove_pdf_split_pdf);
        pdfName=view.findViewById(R.id.pdfName_split_pdf);
        pdfMetaData=view.findViewById(R.id.pdfMetadata_split_pdf);
        uploadPdf=view.findViewById(R.id.uploadPdfLayout);
        //

        selectMorePage=view.findViewById(R.id.selectMorePage);
        pageRangeLayout=view.findViewById(R.id.pageRangeLayout);
        quickLayout=view.findViewById(R.id.quickLayout);


        //

        selectSplitMethod=view.findViewById(R.id.choose_split_method_text);
        splitCard=view.findViewById(R.id.split_card);
        pagePreview=view.findViewById(R.id.pagePreview);
        splitPDFBtn=view.findViewById(R.id.splitPdfBtn);
        no_pdf_selected=view.findViewById(R.id.no_pdf_selected_split_pdf);
        pageCountTxt=view.findViewById(R.id.pageCountTxt);







        backArrow.setOnClickListener(v -> {
            requireActivity().finish();
        });

        pdf_preview.setOnClickListener(v -> {
            Intent intent= new Intent(requireContext(), PdfViewerActivity.class);
            intent.putExtra("pdf_uri",list.get(0).getUri());
            intent.putExtra("mode","view");
            startActivity(intent);

        });

        remove_pdf.setOnClickListener(v -> {
            list.clear();
            updateUi();
        });

        uploadPdf.setOnClickListener(v -> {
            Intent intent=PDF_Picker.pick_pdf();
            pdfLauncher.launch(intent);

        });

        extractPagesRanges.setOnClickListener(v -> {

            btnSelected_for_SplitPDF[0]=0;
            changeVisibility(true);

        });

        individualPagesCard.setOnClickListener(v -> {

            btnSelected_for_SplitPDF[0]=1;
            changeVisibility(false);

        });




         // for the edit text inputs
        floatingEditText=view.findViewById(R.id.floatingEditTextBox);
        floatingframeLayout=view.findViewById(R.id.floatingEditBoxFrameLayout);
        fromPageEditBox=view.findViewById(R.id.fromPageEt);
        toPageEditBox=view.findViewById(R.id.toPageEt);
        ConstraintLayout root=view.findViewById(R.id.parent_layout_split_pdf);

        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            Rect rect = new Rect();
            root.getWindowVisibleDisplayFrame(rect);
            int screenHeight = root.getRootView().getHeight();
            keyBoardHeight = screenHeight - rect.bottom;


            if (keyBoardHeight>screenHeight*0.15){
                floatingframeLayout.setTranslationY(-keyBoardHeight);
                floatingframeLayout.setVisibility(View.VISIBLE);
            }
            else{
                floatingframeLayout.setVisibility(View.GONE);
            }

        });

        fromPageEditBox.setOnFocusChangeListener((v,hasFocus) -> {

            if (hasFocus){
                floatingEditText.setText("");
                moveTypingToFloatingEditTextBox(fromPageEditBox);

            }


        });

        toPageEditBox.setOnFocusChangeListener( (v,hasFocus) -> {

        if (hasFocus){
            floatingEditText.setText("");
            moveTypingToFloatingEditTextBox(toPageEditBox);

        }


        });


        // rangeSlider
        rangeSlider=view.findViewById(R.id.rangeSlider);
        rangeText=view.findViewById(R.id.rangeTxt);

        // to make the popup range in the integer
        rangeSlider.setLabelFormatter(value -> String.valueOf(Math.round(value)));

        rangeSlider.addOnChangeListener((slider,value,fromUser) -> {

            int startPage = Math.round(slider.getValues().get(0));
            int endPage = Math.round(slider.getValues().get(1));

            rangeText.setText(startPage + " – "+endPage+ " of "+list.get(0).getPageCount());

            fromPageEditBox.setText(startPage+"");
            toPageEditBox.setText(endPage+"");


        });


        // pdf page preview recyclerView setup

        pdf_page_preview_recyclerView=view.findViewById(R.id.pages_preview_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);

        pdf_page_preview_recyclerView.setLayoutManager(layoutManager);


        //

        splitPDFBtn.setOnClickListener(v -> {


            if (btnSelected_for_SplitPDF[0]==0) {

                int fromPage=Integer.parseInt(fromPageEditBox.getText().toString());
                int toPage= Integer.parseInt(toPageEditBox.getText().toString());

                if (fromPage==1 && toPage==list.get(0).getPageCount()){
                    Toast.makeText(requireContext(),"Selected range includes all pages.",Toast.LENGTH_LONG).show();
                    return;
                }

                listener.onSplitPDFClick(list.get(0).getUri(), fromPage, toPage,list.get(0).getName());



            }
            else if (btnSelected_for_SplitPDF[0]==1) {

                listener.onSplitPDFClick(list.get(0).getUri(),Integer.MIN_VALUE,Integer.MIN_VALUE,null);
            }


        });


        pickPDF();
        hideMainUi(true);

    }


    private  void updateUi(){

        if (!list.isEmpty()){

            pdfName.setText(list.get(0).getName());
            String pdf_size= Byte_MB.convertByte(list.get(0).getFileSize());
            String pdfPages= list.get(0).getPageCount()+" Pages";
            pdfMetaData.setText(String.format("%s  •  %s",pdf_size,pdfPages));
            addTextWatcherAndAllToEditBox();
            toPageEditBox.setText(list.get(0).getPageCount()+"");
            hideMainUi(false);

            pageCountTxt.setText(String.format(getString(R.string.total_d_pages_will_be_extracted),getTotalPagesFromEditBox()));

             // settings up the value in the rangeSlider

            rangeSlider.setValueFrom(1f);
            rangeSlider.setValueTo( (float) list.get(0).getPageCount() );
            rangeSlider.setValues(1f, (float) list.get(0).getPageCount());

            for (int i=1;i<=list.get(0).getPageCount();i++) previewPages.add(i);

            try{
                ParcelFileDescriptor pfd=requireContext()
                        .getContentResolver()
                        .openFileDescriptor(list.get(0).getUri(),"r");

                PdfRenderer pdfRenderer=new PdfRenderer(pfd);

                adapter=new Split_PDF_Adap_1(previewPages,pdfRenderer);

                pdf_page_preview_recyclerView.setAdapter(adapter);


            }
            catch (IOException e){
                Toast.makeText(requireContext(),"Can't Fetch the PDF Pages",Toast.LENGTH_SHORT).show();
            }




        }
        else {

            hideMainUi(true);

        }


    }
    private void changeVisibility(boolean extractRangeSelected){

        hide_unhideElements(!extractRangeSelected);

        updateCard(
                extractPagesRanges,
                extractPagesRangesText,
                extractPagesRangesIcon,
                extractRangeSelected
        );

        updateCard(
                individualPagesCard,
                individualPagesCardText,
                individualPagesIcon,
                !extractRangeSelected
        );
    }
    private void hide_unhideElements(boolean hide){
            selectMorePage.setVisibility( hide ? View.GONE : View.VISIBLE);
            pageRangeLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
            quickLayout.setVisibility(hide ? View.GONE : View.VISIBLE);

    }
    private void updateCard(View card, TextView text, ImageView icon, boolean selected){

        card.setBackgroundResource(
                selected
                        ? R.drawable.bg_selected_split_card
                        : R.drawable.bg_unselected_split_card
        );

        text.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        selected
                                ? R.color.pink_split_pdf
                                : R.color.black
                )
        );

        icon.setColorFilter(
                ContextCompat.getColor(
                        requireContext(),
                        selected
                                ? R.color.pink_split_pdf
                                : R.color.split_pdf_gray
                )
        );
    };
    private void pickPDF(){
        pdfLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),

                result -> {
                    if (result.getResultCode()==RESULT_OK ){

                        Intent data=result.getData();
                        if (data==null) return;

                        if (data.getData()!=null){
                            Uri uri=data.getData();
//                            loadingBar.setVisibility(View.VISIBLE);
//                            loadingBarText.setText(R.string.fetching_metadata);
//                            Executors.newSingleThreadExecutor().execute(() -> {

                                String name = com.vertlix.utils.file.GetFileName.getFileName(requireContext(), uri);
                                long size = com.vertlix.utils.file.File_Size.getFileSize(requireContext(),uri);
                                int pages = Page_Count.getPdfPageCount(requireContext(),uri);


                                if (pages<=1){
                                    Toast.makeText(requireContext(),"This PDF has only 1 page. Please select a PDF with at least 2 pages.",Toast.LENGTH_LONG).show();
                                    return;
                                }

//                                requireActivity().runOnUiThread(() -> {
                                    list.clear();
//                                    loadingBar.setVisibility(View.GONE);

                                    list.add(new FileItem(
                                            name,
                                            uri,
                                            size,
                                            pages
                                    ));

                                    updateUi();
//                                });
//                            });
                        }
                    }
                }
        );
    }
    private void moveTypingToFloatingEditTextBox(EditText originalEditText) {


        ///  marking the editBox through which this floating box called

        if (originalEditText.equals(fromPageEditBox)) activeTextBox[0] = 0;
        else activeTextBox[0]=1;


        floatingEditText.setHint(originalEditText.getText().toString());
        floatingEditText.requestFocus();

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.showSoftInput(floatingEditText, InputMethodManager.SHOW_IMPLICIT);
    }
    private void addTextWatcherAndAllToEditBox(){

        int totalPdfPages=list.get(0).getPageCount();




        fromPageEditBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().trim().isEmpty()) {

                    int value = Integer.parseInt(s.toString());
                    int toPageValue=Integer.parseInt(toPageEditBox.getText().toString());

                    if (value < 1) {

                        fromPageEditBox.setText(1+"");

                        fromPageEditBox.setSelection(fromPageEditBox.getText().length());

                    }
                    else if (value>toPageValue){

                        fromPageEditBox.setText(toPageValue+"");

                        fromPageEditBox.setSelection(fromPageEditBox.getText().length());

                    }

                    pageCountTxt.setText(String.format(getString(R.string.total_d_pages_will_be_extracted),getTotalPagesFromEditBox()));
                }


            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }
        });

        toPageEditBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {

                    int value = Integer.parseInt(s.toString());
                    int fromPageValue=Integer.parseInt(fromPageEditBox.getText().toString());

                    if (value > totalPdfPages) {
                        toPageEditBox.setText(totalPdfPages+"");
                        toPageEditBox.setSelection(toPageEditBox.getText().length());
                    }
                    else if (value <fromPageValue ){

                        toPageEditBox.setText(fromPageValue+"");
                        toPageEditBox.setSelection(toPageEditBox.getText().length());
                    }
                }

                pageCountTxt.setText(String.format(getString(R.string.total_d_pages_will_be_extracted),getTotalPagesFromEditBox()));


            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });

        floatingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                String currentText=floatingEditText.getText().toString().trim();

                if (currentText.isEmpty()) return;

                 if (activeTextBox[0]==0) fromPageEditBox.setText(currentText);

                 else if (activeTextBox[0]==1) toPageEditBox.setText(currentText);


            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });



    }
    private void hideMainUi(boolean hide){

        int visibility = hide ? View.GONE : View.VISIBLE;

        selectSplitMethod.setVisibility(visibility);
        splitPDFBtn.setVisibility(visibility);
        splitCard.setVisibility(visibility);
        pagePreview.setVisibility(visibility);
        pdf_page_preview_recyclerView.setVisibility(visibility);
        quickLayout.setVisibility(visibility);
        selectMorePage.setVisibility(visibility);
        pageRangeLayout.setVisibility(visibility);
        pdf_preview.setVisibility(visibility);


        no_pdf_selected.setVisibility(hide?View.VISIBLE:View.GONE);


    }
    private  int getTotalPagesFromEditBox(){

        int startingPage=Integer.parseInt(fromPageEditBox.getText().toString());
        int endingPage=Integer.parseInt(toPageEditBox.getText().toString());

        return endingPage-startingPage+1;

    }



    public interface  onSplitPDF{
        void onSplitPDFClick(Uri pdfUri , int fromPage , int toPage,String original_pdf_name);
    }

}