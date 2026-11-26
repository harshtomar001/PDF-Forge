package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.MoreToolsAdapter;
import com.harsh.pdfstudio.model.CardItem;
import com.harsh.pdfstudio.utils.File_Picker;
import com.harsh.pdfstudio.utils.GridSpacingItemDecoration;
import com.harsh.pdfstudio.utils.PDF_Picker;


import java.util.ArrayList;


public class MoreTools extends AppCompatActivity {

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_tools_page);

        final ArrayList<CardItem> list=new ArrayList<>();

        CardItem card=new CardItem(R.drawable.icon_view_pdf,"View PDF","", PdfViewerActivity.class);
        card.setNeedIntent(true);
        card.setType("pdf");
        list.add(card);
        list.add(new CardItem(R.drawable.image_to_pdf,"Images to PDF","Convert Images to PDF", CreatePDF.class));
        list.add( new CardItem(R.drawable.merge_pdf ,"Merge PDF","Combine multiple PDF files", MergePdfActivity.class));
        list.add( new CardItem(R.drawable.compress_pdf,"Compress PDF","Reduce PDF file size", CompressPDF.class));
        list.add( new CardItem(R.drawable.edit_pdf,"Edit PDF","Edit PDF Pages", EditPDF.class));
        list.add( new CardItem(R.drawable.rotate_pdf,"Rotate PDF" ,"Rotate PDF Pages",RotatePDF.class));
        list.add( new CardItem(R.drawable.pdf_to_image,"PDF to Images","Convert PDF  to Images",EditPDF.class));
        list.add(new CardItem(R.drawable.split_pdf,"Split PDF","Split PDF into multiples files", SplitPDF.class));
        list.add(new CardItem(R.drawable.reverse_pdf_icon,"Reverse PDF","Reverse the order of pages of the PDF",ReversePDF.class));
        list.add(new CardItem(R.drawable.ppt_to_pdf_icon,"PPT to PDF","Convert the  PPTX to  PDF",Pptx_To_Pdf.class));
        CardItem card1=new CardItem(R.drawable.icon_view_doc,"View Doc","", DocxViewerActivity.class);
        card1.setNeedIntent(true);
        card1.setType("doc");
        list.add(card1);
        list.add(new CardItem(R.drawable.doc_to_pdf_icon,"DOC to PDF","Convert the  DOCX to  PDF",Docx_to_Pdf.class));
        list.add(new CardItem(R.drawable.icon_convert_image,"Convert Image's Type","", EditImage.class));



        RecyclerView recyclerView=findViewById(R.id.recyclerView_tools);

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float screenWidthDp = metrics.widthPixels / metrics.density;

        int spanCount = Math.max(1, (int) ((screenWidthDp - 22) / 136));


        float remainingDp = (screenWidthDp - 22) - (spanCount * 130);


        float innerDp = remainingDp * 0.65f;
        float edgeDp = remainingDp - innerDp;


        float btwCardSpacingDp = spanCount > 1 ? innerDp / (spanCount - 1) : 0;
        float boundarySpacingDp = edgeDp / 2;


        int btwCardSpacingPx = (int) (btwCardSpacingDp * metrics.density);
        int boundarySpacingPx = (int) (boundarySpacingDp * metrics.density);


        MoreToolsAdapter adapter=new MoreToolsAdapter(list);
        recyclerView.setClipToPadding(false);
        recyclerView.setPadding(boundarySpacingPx, 0,boundarySpacingPx, 0);
         recyclerView.setLayoutManager(new GridLayoutManager(this,spanCount));
        recyclerView.addItemDecoration(
                new GridSpacingItemDecoration(spanCount, btwCardSpacingPx, false)
        );
        recyclerView.setAdapter(adapter);



        adapter.setOnClick_pdf_doc((item) -> {

           if (item.equals("pdf")){
               Intent intent=new PDF_Picker().pick_pdf();
                filePickerLauncher.launch(intent);
           }
           else{
               Intent intent = new File_Picker().doc_picker();
               filePickerLauncher.launch(intent);
           }



        });

        filePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                                Uri uri = result.getData().getData();
                                if (uri == null) return;


                                getContentResolver().takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );

                                String mime=getContentResolver().getType(uri);

                                if ("application/pdf".equals(mime)){

                                    Intent intent = new Intent(this, PdfViewerActivity.class);
                                    intent.putExtra("pdf_uri", uri);
                                    startActivity(intent);


                                }
                                else{
                                    Intent intent = new Intent(this, DocxViewerActivity.class);
                                    intent.putExtra("file_uri", uri);
                                    startActivity(intent);

                                }



                            }
                        }


                );

    }





}




