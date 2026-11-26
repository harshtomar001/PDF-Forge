package com.harsh.pdfstudio.ui;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.EditPDF_Adapter;
import com.harsh.pdfstudio.adapter.PDF_Created_Adapter;
import com.harsh.pdfstudio.fragments.Edit_pdf_Inp_frag;
import com.harsh.pdfstudio.model.FileItem;
import java.util.ArrayList;

public class EditPDF extends AppCompatActivity {

    private ActivityResultLauncher<Intent> pdfLauncher;
    private ActivityResultLauncher<Intent> pickImagesLauncher;
    ArrayList<FileItem> list=new ArrayList<>();
    ArrayList<FileItem> outputlist=new ArrayList<>();
    EditPDF_Adapter pdfadapter;

    PDF_Created_Adapter outputPDF;
    ArrayList<Uri> Imagelist  =new ArrayList<>();

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.parent_page);


        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());

        controller.hide(WindowInsetsCompat.Type.statusBars());

        if (savedInstanceState==null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container,new Edit_pdf_Inp_frag())
                    .commit();
        }


//        pdfadapter=new EditPDF_Adapter(list);
//
//        previewAdapter=new ImageAdapter(Imagelist);
//
//        outputPDF=new PDF_Created_Adapter(outputlist);
//
//
//
//
//
//
//        FrameLayout loading=findViewById(R.id.loading_overlay1);
//        TextView text=loading.findViewById(R.id.text_LoadingBar);
//        text.setText("Fetching Images...");
//
//
//        ImageView backArrow=findViewById(R.id.back_arrow_edit_page);
//
//        MaterialButton upload_btn=findViewById(R.id.upload_btn_edit_pdf);
//
//        MaterialButton generate_btn=findViewById(R.id.generate_pdf_edit_pdf);
//
//        RecyclerView recyclerView=findViewById(R.id.recycler_view_edit_pdf);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        recyclerView.setAdapter(pdfadapter);
//
//        RecyclerView recyclerView_output=findViewById(R.id.recycler_view_output_pdf);
//
//        recyclerView_output.setLayoutManager(new LinearLayoutManager(this));
//
//        recyclerView_output.setAdapter(outputPDF);
//
//        RecyclerView preview_Images=findViewById(R.id.recyclerView_preview_images);
//
//        preview_Images.setLayoutManager(new GridLayoutManager(this,3));
//
//        preview_Images.setAdapter(previewAdapter);
//
//        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
//        touchHelper.attachToRecyclerView(preview_Images);
//
//
//
//
//
//        pdfadapter.setOnPdfRemovedListener(() -> {
//            Imagelist.clear();
//            previewAdapter.notifyDataSetChanged();
//        });
//
//        previewAdapter.onAddImageClickListener(() ->{
//            Intent intent= Image_picker.openImageFiles();
//            pickImagesLauncher.launch(intent);
//
//        });
//
//
//
//        backArrow.setOnClickListener(v -> {
//            finish();
//        });
//
//        upload_btn.setOnClickListener(v ->{
//
//            Intent intent= PDF_Picker.pick_pdf();
//            pdfLauncher.launch(intent);
//
//
//        });
//
//        generate_btn.setOnClickListener(v ->{
//
//            String fileName="pdf_"+System.currentTimeMillis();
//
//            ExecutorService executors= Executors.newSingleThreadExecutor();
//            Handler handler=new Handler(Looper.getMainLooper());
//            loading.setVisibility(View.VISIBLE);
//
//            executors.execute(() ->{
//
//                ArrayList<Uri> images = new ArrayList<>(Imagelist.subList(0, Imagelist.size() - 1));
//
//                File pdf = null;
//                try {
//                    pdf = generatePDF.createPdf(this,images,fileName);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//
//                Uri PDF_uri= FileProvider.getUriForFile(
//                        this,
//                        getPackageName() + ".provider",
//                        pdf
//                );
//
//                String PDFName= GetFileName.getFileName(this,PDF_uri);
//
//                long PDFSize= File_Size.getFileSize(this,PDF_uri);
//
//                int PDF_Page= Page_Count.getPdfPageCount(this,PDF_uri);
//
//                handler.post(() ->{
//                    outputlist.add(new FileItem(PDFName,PDF_uri,PDFSize,PDF_Page));
//                    outputPDF.notifyDataSetChanged();
//                    Imagelist.clear();
//                    list.clear();
//                    pdfadapter.notifyDataSetChanged();
//                    previewAdapter.notifyDataSetChanged();
//                    loading.setVisibility(View.GONE);
//
//
//                });
//
//
//
//
//
//
//            });
//
//
//        });

//        pickImagesLauncher =
//                registerForActivityResult(
//                        new ActivityResultContracts.StartActivityForResult(),
//                        result -> {
//                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//
//                                Intent data = result.getData();
//
//                                if (data.getClipData() != null) {
//
//                                    ClipData clipData = data.getClipData();
//
//                                    for (int i = 0; i < clipData.getItemCount(); i++) {
//                                        Uri uri = clipData.getItemAt(i).getUri();
//                                        Imagelist.add(Imagelist.size()-1, uri);
//                                    }
//
//                                } else if (data.getData() != null) {
//
//                                    Imagelist.add(Imagelist.size()-1, data.getData());
//                                }
//
//                                previewAdapter.notifyDataSetChanged();
//                            }
//                        }
//                );


    }


}
