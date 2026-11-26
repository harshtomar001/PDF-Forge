package com.harsh.pdfstudio.fragments;

import static android.app.Activity.RESULT_OK;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.utils.File_Picker;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.Page_Count;
import org.jspecify.annotations.NonNull;
import java.util.concurrent.Executors;

public class Docx_Pdf_Inp_Frag  extends Pptx_Pdf_Inp_Frag{

    private ActivityResultLauncher<Intent> docLauncher;



    @Override
    public void onViewCreated(@NonNull View view, Bundle savedBundle){

        super.onViewCreated(view,savedBundle);



        title_bg.setBackgroundResource(R.drawable.doc_pdf_toolbar_bg);
        title.setText(R.string.docx_to_pdf);
        uploadFileLayout.setBackgroundResource(R.drawable.dashed_border_doc_pdf);
        upl_file_text.setText(R.string.upload_pptx_to_convert_to_pdf);
        upl_file_title.setText(R.string.upload_doc);
        upl_file_title.setTextColor(ContextCompat.getColor(requireContext(),R.color.doc_blue));
        iconImage.setImageResource(R.drawable.doc_icon_blue);
        noPPT_selected.setImageResource(R.drawable.doc_img);
        convertToPDF.setBackgroundResource(R.drawable.doc_to_pdf_btn_bg);
        convrtTopdf_text.setText(R.string.convert_doc_files_to_pdf);
        pdfIcon.setImageResource(R.drawable.pdf_icon_white_blue);
        shieldImage.setImageResource(R.drawable.blue_shield_icon);
        shieldLayout.setBackgroundResource(R.drawable.sheild_bg_doc_pdf);
        ppt_doc_Icon.setImageResource(R.drawable.doc_preview_blue);
        pptRemoved.setImageResource(R.drawable.cross_icon_blue);
        ready_to_cnvrt.setImageResource(R.drawable.ready_to_convert_doc);






        uploadFileLayout.setOnClickListener(v -> {
            Intent intent= File_Picker.doc_picker();
            docLauncher.launch(intent);
        });

        docPicker();

    }


    private  void docPicker(){
        docLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),

                result -> {
                    if (result.getResultCode()==RESULT_OK ){

                        Intent data=result.getData();
                        if (data==null) return;

                        if (data.getData()!=null){
                            Uri uri=data.getData();
                            loadingBar.setVisibility(View.VISIBLE);
                            Executors.newSingleThreadExecutor().execute(() -> {

                                String name = GetFileName.getFileName(requireContext(), uri);
                                long size = File_Size.getFileSize(requireContext(), uri);
                                int pages = Page_Count.getDocxPageCount(requireContext(), uri);
                                Log.d("pages",pages+"");

                                requireActivity().runOnUiThread(() -> {
                                    list.clear();
                                    loadingBar.setVisibility(View.GONE);
                                    list.add(new FileItem(
                                            name,
                                            uri,
                                            size,
                                            pages
                                    ));

                                    updateUI("doc"); // inherited
                                });
                            });


                        }

                    }

                }

        );
    }


}
