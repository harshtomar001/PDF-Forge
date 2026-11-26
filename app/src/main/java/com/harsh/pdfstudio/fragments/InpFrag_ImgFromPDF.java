package com.harsh.pdfstudio.fragments;

import static android.app.Activity.RESULT_OK;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.PDF_Picker;
import com.harsh.pdfstudio.utils.Page_Count;
import java.util.ArrayList;

public class InpFrag_ImgFromPDF  extends Fragment {

    TextView title;
    TextView subtitle;
    MaterialAutoCompleteTextView quality;
    MaterialAutoCompleteTextView format;
    RadioButton allPages;
    RadioButton customPages;
    ArrayList<FileItem> list =new ArrayList<>();
    LinearLayout uploadPDF;
    ActivityResultLauncher<Intent> pdfLauncher;
    ImageView pdfLogo;
    ImageView removePDF;
    TextView pdfName;
    TextView pdfPages;
    TextView pdfSizes;
    LinearLayout customRangeLayout;
    MaterialCardView pdfPreview;
    TextView option;
    MaterialCardView outputDescription;
    MaterialCardView extractImages;
    ImageView noPdf_Selected;
    TextInputEditText firstPages;
    TextInputEditText lastPages;
    EditText floatingText_KeyBoard;
    FrameLayout floatingFrameLayout;
    int pages;
    private  ExtractImages listener;
    private int keyBoardHeight;
    int[] activeTextBox= new int[]{1};
    public  void setOnClickExtractImage(ExtractImages listener){
        this.listener=listener;

    }

    public InpFrag_ImgFromPDF(){

        super(R.layout.extract_img_inp_frag);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        super.onViewCreated(view,savedInstanceState);

        title=view.findViewById(R.id.title); // title text
        subtitle=view.findViewById(R.id.subtitle_fetch_img_pdf); //  text below title
        quality=view.findViewById(R.id.quality_dropdown); //  choosing image's  quality  -> dropdown menu
        format=view.findViewById(R.id.format_dropdown); //  choosing format of img  -> dropdown menu
        allPages=view.findViewById(R.id.all_pages);    //  all pages radiobutton
        customPages=view.findViewById(R.id.custom_range); // custom range radiobutton
        uploadPDF=view.findViewById(R.id.upload_pdf); // upload pdf button
        pdfName=view.findViewById(R.id.pdfName_ex_img);// pdf name in the  pdf preview container
        pdfPages=view.findViewById(R.id.pdf_pages_ex_img);// pdf pages in the  pdf preview container
        pdfSizes=view.findViewById(R.id.pdf_sizes_ex_img);// pdf size in the  pdf preview container
        pdfLogo=view.findViewById(R.id.pdf_image); // pdf logo
        removePDF=view.findViewById(R.id.croee_ex_img); // cross button for pdf removal
        customRangeLayout=view.findViewById(R.id.from_to_layout); //  container of   of edit input box of the custom layout
        extractImages=view.findViewById(R.id.extractImg_cardView); // extracting images button
        outputDescription=view.findViewById(R.id.output_info); // content card of pages, custom ranges ....
        option=view.findViewById(R.id.text); // just text option
        pdfPreview=view.findViewById(R.id.pdf_preview_ex_img); // pdf preview (card)
        noPdf_Selected=view.findViewById(R.id.no_pdf_selected);// for no pdf images selected img
        firstPages=view.findViewById(R.id.firstPages); // edit input text box for custom range (1 pages column)
        lastPages=view.findViewById(R.id.totalPages); // edit input text box for custom range (last pages column)
        floatingText_KeyBoard=view.findViewById(R.id.floatingEdit); // the edit box which appear when keyboard appear
        floatingFrameLayout=view.findViewById(R.id.floatingFrameLayout); // frame layout of floatingText_keyboard


        LinearLayout root = view.findViewById(R.id.parent_layout);


        /// check weather keyboard is open or not // run every time when ui refreshes

        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            Rect rect = new Rect();
            root.getWindowVisibleDisplayFrame(rect);
            int screenHeight = root.getRootView().getHeight();
            keyBoardHeight = screenHeight - rect.bottom;


            if (keyBoardHeight>screenHeight*0.15){
                floatingFrameLayout.setTranslationY(-keyBoardHeight);
                floatingFrameLayout.setVisibility(View.VISIBLE);
            }
            else{
                floatingFrameLayout.setVisibility(View.GONE);
            }

        });


        String[] qualityItems={"Original Quality","High","Medium","Low"};
        String[] formatItems={"Original Format","JPG","PNG","WEBP"};

        ArrayAdapter<String> qualityAdapter=new ArrayAdapter<>(requireContext(), R.layout.dropdown_item,qualityItems);
        ArrayAdapter<String> formatAdapter=new ArrayAdapter<>(requireContext(), R.layout.dropdown_item,formatItems);

        quality.setAdapter(qualityAdapter);
        format.setAdapter(formatAdapter);

        format.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.dropdown_bg));
        quality.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.dropdown_bg));

        quality.setDropDownWidth(500);
        format.setDropDownWidth(500);

        noPDfSelected(true);

        quality.setOnClickListener(v -> {

            quality.showDropDown();
        });

        format.setOnClickListener(v ->{

         format.showDropDown();
        });

        uploadPDF.setOnClickListener(v-> {

            Intent intent= PDF_Picker.pick_pdf();
            pdfLauncher.launch(intent);


        });


        allPages.setChecked(true);

        title.setText(R.string.extract_images);
        subtitle.setText(R.string.fetch_images_from_pdf);


        ImageView backArrow=view.findViewById(R.id.back_arrow_edit_image);

        backArrow.setOnClickListener((v) -> {
            getActivity().finish();

        });

        extractImages.setOnClickListener(v ->{

            int first=1;
            int last=pages;

            if (customPages.isChecked()) {

                first=Integer.parseInt(String.valueOf(firstPages.getText()));
                last=Integer.parseInt(String.valueOf(lastPages.getText()));

                if (first>last){
                    Toast.makeText(requireContext(),"Invalid Range",Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            listener.extractImages(first, last, list.get(0).getUri(), format.getText().toString(), quality.getText().toString());


        } );



       pdfPreview.setOnClickListener( v -> {

           Intent intent=new Intent(requireContext(), PdfViewerActivity.class);
            intent.putExtra("pdf_uri",list.get(0).getUri());
            intent.putExtra("mode","view");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            requireContext().startActivity(intent);

       });

       pdfLogo.setOnClickListener(v -> {});

       removePDF.setOnClickListener(v -> {

           list.clear();
           noPDfSelected(true);

       });


        allPages.setOnClickListener(v -> {

            customPages.setChecked(false);
            customRange();

        });

        customPages.setOnClickListener( v -> {

            allPages.setChecked(false);
            customRange();

        });


        pdfPicker();


    }



    private  void pdfPicker(){

        pdfLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),

                result -> {
                    if (result.getResultCode()==RESULT_OK ){

                        Intent data=result.getData();
                        if (data==null) return;

                        if (data.getData()!=null){
                            Uri uri=data.getData();

                            list.clear();
                            list.add(new FileItem(GetFileName.getFileName(requireContext(),uri),
                                    uri,
                                    File_Size.getFileSize(requireContext(),uri),
                                    Page_Count.getPdfPageCount(requireContext(),uri)));
                            pages=list.get(0).getPageCount();
                            updateUI();
                            noPDfSelected(false);
                            customPages.setChecked(false);
                            customRange();


                        }

                    }

                }

        );
    }

    private void customRange(){

        if (customPages.isChecked()) {

            customRangeLayout.setVisibility(View.VISIBLE);

        }
        else{

            customRangeLayout.setVisibility(View.GONE);

        }
    }

    private  void noPDfSelected(boolean hide){

        if (hide){
            pdfPreview.setVisibility(View.GONE);
            option.setVisibility(View.GONE);
            extractImages.setVisibility(View.GONE);
            outputDescription.setVisibility(View.GONE);
            noPdf_Selected.setVisibility(View.VISIBLE);


        }else{

            pdfPreview.setVisibility(View.VISIBLE);
            option.setVisibility(View.VISIBLE);
            extractImages.setVisibility(View.VISIBLE);
            outputDescription.setVisibility(View.VISIBLE);
            noPdf_Selected.setVisibility(View.GONE);

        }



    };
    public interface  ExtractImages{ void extractImages(int first, int last, Uri uri, String format,String  quality);}


    private  void updateUI(){

        allPages.setChecked(true); // set the radiobutton on  (By default it  all pages radiobutton is checked)
        pdfName.setText(list.get(0).getName()); //  updating the pdf name in the pdf preview
        pdfSizes.setText(Byte_MB.convertByte(list.get(0).getFileSize())); // updating the pdf sizes in the pdf preview
        pdfPages.setText(pages+" Pages"); // updating the pdf pages in the pdf preview
        firstPages.setText("1"); //  setting the  starting page no in the custom range textbox
        lastPages.setText(pages+""); //  setting the last page no in the custom range  textbox


        /// now adding the textWatcher to the firstPage editBox so it remain within the range using textWatcher

        firstPages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().trim().isEmpty()) {

                    int value = Integer.parseInt(s.toString());

                    if (value < 1) {

                        firstPages.setText(1+"");

                        firstPages.setSelection(firstPages.getText().length());

                    }
                    else if (value>pages){

                        firstPages.setText(pages+"");

                        firstPages.setSelection(firstPages.getText().length());

                    }
                }
            }
        }); // set the minimum  and maximum limit

        /// now adding the textWatcher to the firstPage editBox so it remain within the range using textWatcher

        lastPages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {

                    int value = Integer.parseInt(s.toString());

                    if (value > pages) {
                        lastPages.setText(pages+"");
                        lastPages.setSelection(lastPages.getText().length());
                    }
                    else if (value < 1){

                        lastPages.setText(1+"");
                        lastPages.setSelection(lastPages.getText().length());
                    }
                }
            }
        }); // set the maximum and minimum limit

        /// calling the floating editBox if user click the firstPages textBox

        firstPages.setOnFocusChangeListener((v ,hasFocus) -> {
            if (hasFocus){
                floatingText_KeyBoard.setText("");
                moveTypingToFloating(firstPages);

            }
        });

        /// calling the floating editBox if user click the lastPages textBox

        lastPages.setOnFocusChangeListener((v ,hasFocus) -> {
            if (hasFocus){
                floatingText_KeyBoard.setText("");
                moveTypingToFloating(lastPages);

            }
        });

        floatingText_KeyBoard.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

                String floatingText = floatingText_KeyBoard.getText().toString().trim();

                if (floatingText == null || floatingText.length() == 0) return;

                ///  inserting the value to the  respective editBox if user write something  in the floating box

                if (activeTextBox[0] == 0) firstPages.setText(floatingText);
                else if (activeTextBox[0] == 1) lastPages.setText(floatingText);


            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        });

    };

    private void moveTypingToFloating(TextInputEditText originalEditText) {


        ///  marking the editBox through which this floating box called

        if (originalEditText.equals(firstPages)) activeTextBox[0] = 0;
        else activeTextBox[0]=1;


        floatingText_KeyBoard.setHint(originalEditText.getText().toString());
        floatingText_KeyBoard.requestFocus();

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.showSoftInput(floatingText_KeyBoard, InputMethodManager.SHOW_IMPLICIT);
    }

}