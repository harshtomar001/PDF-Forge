package com.harsh.pdfstudio.fragments;

import static android.app.Activity.RESULT_OK;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Picker;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.Page_Count;
import com.harsh.pdfstudio.utils.internetAvailable;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Pptx_Pdf_Inp_Frag extends Fragment {

    protected   ActivityResultLauncher<Intent> pptLauncher;
    MaterialCardView ppt_preview_card;
    LinearLayout title_bg;
    TextView title;
    ImageView back_arrow;
    LinearLayout shieldLayout;
    ImageView shieldImage;
    TextView pptName;
    TextView pptSize;
    TextView pptSlides;
    ImageView ready_to_cnvrt;
    ImageView noPPT_selected;
    ImageView pptRemoved;
    LinearLayout convertToPDF;

    private  onConvertPdf listener;

    ConstraintLayout no_internetLayout;
    ImageView remove_noInternet;
    MaterialButton retryButton;

    ProgressBar progressBar;
    TextView progressBarText;
    View uploadFileLayout;
    ImageView iconImage;
    TextView upl_file_title;
    TextView upl_file_text;
    TextView convrtTopdf_text;
    ImageView pdfIcon;
    ImageView ppt_doc_Icon;

    protected ArrayList<FileItem> list=new ArrayList<>(); // protected is used so that we can use same reference in child classes

    View loadingBar;
    TextView loadingBarText;

    //
    public void onPDFCLick(onConvertPdf listener){
        this.listener=listener;
    }





    public Pptx_Pdf_Inp_Frag(){
        super(R.layout.pptx_pdf_inp_frag);
    }





    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        //
        uploadFileLayout=view.findViewById(R.id.upload_Layout);
        iconImage=view.findViewById(R.id.ppt_image);
        upl_file_title=view.findViewById(R.id.upl_ppt_title);
        upl_file_text=view.findViewById(R.id.upl_ppt_text);
        convrtTopdf_text=view.findViewById(R.id.convrt_to_pdf_text);
        pdfIcon=view.findViewById(R.id.pdf_icon_doc_ppt_pdf);
        ppt_doc_Icon=view.findViewById(R.id.pptPreview);
        loadingBar=view.findViewById(R.id.loading_bar);
        loadingBarText=view.findViewById(R.id.text_LoadingBar);


        //
        title_bg=view.findViewById(R.id.titleLayout);
        title_bg.setBackgroundResource(R.drawable.ppt_pdf_toolbar_bg);
        title=view.findViewById(R.id.textView_crt_pdf);
        title.setText(R.string.pptx_to_pdf);
        back_arrow=view.findViewById(R.id.back_arrow);

        // for shield  elements
        shieldLayout=view.findViewById(R.id.bottom_section);
        shieldImage=view.findViewById(R.id.shield_imageView);
        shieldImage.setImageResource(R.drawable.shield_check_icon_orange);
        shieldLayout.setBackgroundResource(R.drawable.shield_bg_ppt_pdf);

        //

        //
        ppt_preview_card=view.findViewById(R.id.card_layout);
        pptName=view.findViewById(R.id.pptName);
        pptSize=view.findViewById(R.id.pptSize);
        pptSlides=view.findViewById(R.id.pptSlides);
        ready_to_cnvrt=view.findViewById(R.id.ready_to_convrt);
        noPPT_selected=view.findViewById(R.id.no_ppt_selected);
        pptRemoved=view.findViewById(R.id.remove_ppt);
        convertToPDF=view.findViewById(R.id.convert_to_pdf);
        no_internetLayout=view.findViewById(R.id.noInternetLayout);
        remove_noInternet=view.findViewById(R.id.removeImage);
        retryButton=view.findViewById(R.id.retryButton);
        progressBar=view.findViewById(R.id.checkingProgress);
        progressBarText=view.findViewById(R.id.checkingText);





        back_arrow.setOnClickListener( v ->{
            requireActivity().finish();

        });

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().finish();
                    }
                }
        );




        uploadFileLayout.setOnClickListener(v -> {
            Intent intent= File_Picker.ppt_picker();
            pptLauncher.launch(intent);


        });

        pptRemoved.setOnClickListener(v -> {
            list.clear();
            updateUI("ppt");
        });


        remove_noInternet.setOnClickListener(v -> {
            no_internetLayout.setVisibility(View.GONE);
        });

        retryButton.setOnClickListener(v -> {

                progressBar.setVisibility(View.VISIBLE);
                progressBarText.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.GONE);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                boolean result =internetAvailable.hasActiveInternet(requireContext());

                if(result){
                    no_internetLayout.setVisibility(View.GONE);
                    listener.convertToPdf(list.get(0).getUri(),list.get(0).getName());
                    list.clear();

                }
                else{
                    retryButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    progressBarText.setVisibility(View.GONE);

                }

            },1500);

        });


        convertToPDF.setOnClickListener(v -> {


            if (list.size()==0){
                Toast.makeText(requireContext(),"Select PPT file",Toast.LENGTH_SHORT).show();
                return;
            }
            if (!internetAvailable.hasActiveInternet(requireContext())){
                no_internetLayout.setVisibility(View.VISIBLE);
                return;
            }
            listener.convertToPdf(list.get(0).getUri(),list.get(0).getName());
            list.clear();
        });





        pptPicker();




    }

    protected void updateUI(String type){
        if (list.size()==0){

            ppt_preview_card.setVisibility(View.GONE);
            ready_to_cnvrt.setVisibility(View.GONE);
            noPPT_selected.setVisibility(View.VISIBLE);

        }else{
            ppt_preview_card.setVisibility(View.VISIBLE);
            ready_to_cnvrt.setVisibility(View.VISIBLE);
            noPPT_selected.setVisibility(View.GONE);

            String name=list.get(0).getName().length()>23?list.get(0).getName().substring(0,22)+"...":list.get(0).getName();
            String ppt_size= Byte_MB.convertByte(list.get(0).getFileSize());
            String slides=list.get(0).getPageCount() +   (type.equals("doc")? " Pages" : " Slides");
            pptName.setText(name);
            pptSize.setText(ppt_size);
            pptSlides.setText(slides);




        }

    }

    private  void pptPicker(){
        pptLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),

                result -> {
                    if (result.getResultCode()==RESULT_OK ){

                        Intent data=result.getData();
                        if (data==null) return;

                        if (data.getData()!=null){
                            Uri uri=data.getData();
                            loadingBar.setVisibility(View.VISIBLE);
                            loadingBarText.setText(R.string.fetching_metadata);
                            Executors.newSingleThreadExecutor().execute(() -> {

                                String name = GetFileName.getFileName(requireContext(), uri);
                                long size = File_Size.getFileSize(requireContext(), uri);
                                int slides = Page_Count.getPptxSlideCount(requireContext(), uri);

                                requireActivity().runOnUiThread(() -> {
                                    list.clear();
                                    loadingBar.setVisibility(View.GONE);

                                    list.add(new FileItem(
                                            name,
                                            uri,
                                            size,
                                            slides
                                    ));

                                    updateUI("ppt");
                                });
                            });
                        }
                    }
                }
        );
    }

    public interface  onConvertPdf{
        void convertToPdf(Uri pptUri,String ppt_name);
    }
}