package com.harsh.pdfstudio.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.RotatedAdapter;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.PDF_Picker;
import com.harsh.pdfstudio.utils.Page_Count;
import com.harsh.pdfstudio.utils.PdfToFilterImgUri;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseInpFragment_rev_rot_pdf extends Fragment {

    protected ActivityResultLauncher<Intent> pdfLauncher;
    protected  ArrayList<FileItem> list=new ArrayList<>(); // for selecting pdfs
    protected  ArrayList<Uri> prev_imagesUri;
    protected TextView fileName;
    protected TextView pagesCount;
    protected TextView pdfSize;
    protected ImageView defaultImage;
    protected MaterialCardView pdf_preview;
    protected View recyclerViewLayout;
    protected  LinearLayout uploadImage;
    protected LinearLayout btn_layout;
    protected RecyclerView recyclerView;
    protected RotatedAdapter adapter;
    protected FrameLayout progressBar;
    protected ExecutorService executor= Executors.newSingleThreadExecutor();
    protected  Handler handler=new Handler(Looper.getMainLooper());
    protected  MaterialButton _0_degree;
    protected  MaterialButton _90_degree;
    protected TextView totalPages;
    protected TextView changePDf;
    protected  TextView previewText;
    protected ImageView backArrow;
    protected LinearLayout rotatePDF;
    protected  TextView page_title;
    protected  TextView toolbar_text_title;
    protected  TextView text_below_upl_pdf;
    protected  TextView generate_pdf_text;
    protected  TextView info_text;
    protected  ImageView generate_pdf_icon;


    public BaseInpFragment_rev_rot_pdf(int layoutId) {
        super(layoutId);

    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);


        backArrow=view.findViewById(R.id.back_arrow_rotate_pdf);
        uploadImage=view.findViewById(R.id.rotate_pdf_upload_layout);
        fileName=view.findViewById(R.id.PDFName);
        pagesCount=view.findViewById(R.id.PDFPages);
        pdfSize=view.findViewById(R.id.PDFSize);
        defaultImage=view.findViewById(R.id.no_rotate_pdf);
        pdf_preview=view.findViewById(R.id.pdf_preview);
        recyclerViewLayout=view.findViewById(R.id.recyler_view);
        btn_layout=view.findViewById(R.id.layout);
        recyclerView=view.findViewById(R.id.rotate_pdf_recyclerView);
        progressBar=view.findViewById(R.id.progress_indicator_rotate_pdf);
        _0_degree=view.findViewById(R.id.btn_0_degree);
        _90_degree=view.findViewById(R.id.btn_90_degree);
        previewText=view.findViewById(R.id.preview_text);
        totalPages=view.findViewById(R.id.total_pages_preview);
        changePDf=view.findViewById(R.id.change_pdf);
        rotatePDF=view.findViewById(R.id.rotate_pdf_layout);
        page_title=view.findViewById(R.id.page_title);
        toolbar_text_title=view.findViewById(R.id.title_page_text_toolbar);
        text_below_upl_pdf=view.findViewById(R.id.text_below_upl_pdf);
        generate_pdf_icon=view.findViewById(R.id.generate_pdf_icon);
        generate_pdf_text=view.findViewById(R.id.generatePDF_text);
        info_text=view.findViewById(R.id.info_text);


        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().finish();
                    }
                }
        );

            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(),2));


            uploadImage.setOnClickListener(v -> {

                uploadPdf();

            });

            changePDf.setOnClickListener(v -> {

               uploadPdf();

            });


            setActive(_0_degree,_90_degree);



            backArrow.setOnClickListener( v ->{

                requireActivity().finish();
            });


            _0_degree.setOnClickListener(v ->{
                _0_degree_Click();


            });

            _90_degree.setOnClickListener(v -> {
               _90_degree_Click();

            });

            pdf_preview.setOnClickListener( v -> {
                Intent intent=new Intent(requireContext(), PdfViewerActivity.class);
                intent.putExtra("pdf_uri",list.get(0).getUri());
                intent.putExtra("mode","view");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                requireContext().startActivity(intent);

            });





            pdfPicker();



    }

    protected  void uploadPdf(){
        Intent intent= PDF_Picker.pick_pdf();
        pdfLauncher.launch(intent);
    }
    private  void pdfPicker(){
        pdfLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),

                result -> {
                    if (result.getResultCode()==RESULT_OK ){

                        Intent data=result.getData();
                        if (data==null) return;

                        if (data.getData()!=null){
                            Uri  uri=data.getData();

                            list.clear();
                            list.add(new FileItem(GetFileName.getFileName(requireContext(),uri),
                                    uri,
                                    File_Size.getFileSize(requireContext(),uri),
                                    Page_Count.getPdfPageCount(requireContext(),uri)));
                            updateUI();


                        }

                    }

                }

        );
    }

    protected  void _0_degree_Click(){
        setActive(_0_degree, _90_degree);
        adapter.rotation(0f);
        previewText.setText(getString(R.string.preview_after_rotation_90,0));
    }

    protected  void _90_degree_Click(){
        setActive(_90_degree, _0_degree);
        adapter.rotation(90f);
        previewText.setText(getString(R.string.preview_after_rotation_90,90));
    }


    protected void updateUI(){

        if (list.size()==0){

            defaultImage.setVisibility(View.VISIBLE);
            pdf_preview.setVisibility(View.GONE);
            recyclerViewLayout.setVisibility(View.GONE);
            btn_layout.setVisibility(View.VISIBLE);

            if(adapter != null){
                adapter.updateList(new ArrayList<>());
            }

            return;
        }

        defaultImage.setVisibility(View.GONE);
        pdf_preview.setVisibility(View.VISIBLE);
        recyclerViewLayout.setVisibility(View.VISIBLE);
        btn_layout.setVisibility(View.GONE);

        changePDFPreviewData();




    }


    protected void changePDFPreviewData(){

        ArrayList<Integer> indexes= new ArrayList<>();

        FileItem pdf=list.get(0);
        fileName.setText(pdf.getName().length()>23?pdf.getName().substring(0,19)+"...":pdf.getName());
        pagesCount.setText(pdf.getPageCount()+" Pages");
        pdfSize.setText(Byte_MB.convertByte(pdf.getFileSize()));

        int pageCount=pdf.getPageCount();

        // returning indexes
        if (pageCount<=6){
            for (int i=1;i<=pageCount;i++){
                indexes.add(i);
            }

        }
        else{
            indexes.add(1);// first 3 images
            indexes.add(2);
            indexes.add(3);
            indexes.add(1+(pageCount-1)/2); // mid img
            indexes.add(pageCount-1); // second last
            indexes.add(pageCount);   // last
        }

        totalPages.setText(getString(R.string.showing_n_Pages_from_m_pages,indexes.size(),pageCount));

        loadData(indexes);
    }


    protected void loadData(ArrayList<Integer> indexes_images){

        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setElevation(100f);

        executor.execute(() -> {


            try {
                prev_imagesUri = PdfToFilterImgUri.filteredImages(
                        requireContext(),
                        list.get(0).getUri(),
                        indexes_images
                );

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            handler.post(() -> {


                progressBar.setVisibility(View.GONE);

                adapter = new RotatedAdapter(prev_imagesUri);

                adapter.rotation(0f);
                recyclerView.setAdapter(adapter);
            });
        });

    };


    protected void setActive(MaterialButton active, MaterialButton inactive) {

        int black = ContextCompat.getColor(requireContext(),R.color.purple);
        int white = ContextCompat.getColor(requireContext(), R.color.white);

        // ACTIVE
        active.setBackgroundTintList(ColorStateList.valueOf(black));
        active.setTextColor(white);
        active.setIconTint(ColorStateList.valueOf(white));

        // INACTIVE
        inactive.setBackgroundTintList(ColorStateList.valueOf(white));
        inactive.setTextColor(black);
        inactive.setIconTint(ColorStateList.valueOf(black));
    }


}