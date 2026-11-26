package com.harsh.pdfstudio.fragments;

import static android.app.Activity.RESULT_OK;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.PDF_Picker;
import com.harsh.pdfstudio.utils.Page_Count;
import org.jspecify.annotations.NonNull;
import java.util.ArrayList;


public class Compress_pdf_Inp_Frag extends Fragment {

    ArrayList<FileItem> list=new ArrayList<>();
    ActivityResultLauncher <Intent> pdfLauncher;
    LinearLayout uploadPDF;
    LinearLayout uploadPDF_btn;
    ImageView pdfIcon;
    MaterialCardView pdf_preview;
    ImageView pdfIcon_cardView;
    TextView change_pdf_cardView;
    View lowCard;
    View mediumCard;
    View highCard;
    ConstraintLayout lowCardBg;
    ConstraintLayout mediumCardBg;
    ConstraintLayout highCardBg;
    RadioButton lowRadio;
    RadioButton mediumRadio;
    RadioButton highRadio;
    TextView PDFName;
    TextView PDFSize;
    TextView PDFPages;
    TextView ActualPdfSize;
    TextView EstimatedPdfSize;
    TextView sizeReduction;
    LinearLayout compressPDFBtn;
    ProgressBar compressionProgressbar;
    private onCompress listener;

    View pdf_previewGroup;

    ImageView btnRemovePdf;
    View estimateCard;

    int[] selectedRadio=new int[1];

    public void setonCompressClick(onCompress listener){
        this.listener=listener;
    }

    public Compress_pdf_Inp_Frag(){
        super(R.layout.compress_pdf);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle){

        super.onViewCreated(view,bundle);

        ImageView back_arrow_compress =view.findViewById(R.id.back_arrow_cmp_pdf);
        uploadPDF=view.findViewById(R.id.upl_layout);
        uploadPDF.setBackgroundResource(R.drawable.upl_pdf_bg_cmp_pdf);
        pdfIcon=view.findViewById(R.id.pdf_icon_cmp_ep);
        pdfIcon.setImageResource(R.drawable.pdf_icon_cmp_pdf);
        uploadPDF_btn=view.findViewById(R.id.upload_pdf);
        uploadPDF_btn.setBackgroundResource(R.drawable.upl_pdf_cmp_pdf);
        pdf_preview=view.findViewById(R.id.pdf_preview_cmp_pdf);
        pdfIcon_cardView=view.findViewById(R.id.imgPdfIcon);
        pdfIcon_cardView.setImageResource(R.drawable.pdf_icon_1_cmp_pdf);

        lowCard=view.findViewById(R.id.lowCard);
        mediumCard=view.findViewById(R.id.mediumCard);
        highCard=view.findViewById(R.id.highCard);
        lowCardBg=lowCard.findViewById(R.id.radioBtn_bg);
        mediumCardBg=mediumCard.findViewById(R.id.radioBtn_bg);
        highCardBg=highCard.findViewById(R.id.radioBtn_bg);
        lowRadio=lowCard.findViewById(R.id.compression_radioBtn);
        mediumRadio=mediumCard.findViewById(R.id.compression_radioBtn);
        highRadio=highCard.findViewById(R.id.compression_radioBtn);
        TextView mediumLevel=mediumCard.findViewById(R.id.compression_level);
        TextView highLevel=highCard.findViewById(R.id.compression_level);
        TextView mediumLevelText=mediumCard.findViewById(R.id.compression_text);
        TextView highLevelText=highCard.findViewById(R.id.compression_text);
        TextView mediumQuality=mediumCard.findViewById(R.id.quality);
        TextView highQuality=highCard.findViewById(R.id.quality);
        mediumLevel.setText(R.string.medium_compression);
        highLevel.setText(R.string.high_compression);
        mediumLevelText.setText(R.string.good_quality_medium_size);
        highLevelText.setText(R.string.lower_quality_small_size);
        mediumQuality.setText(R.string._70_quality);
        highQuality.setText(R.string._40_quality);
        mediumQuality.setBackgroundResource(R.drawable.quality_70_bg);
        mediumQuality.setTextColor(ContextCompat.getColor(requireContext(),R.color._70_quality));
        highQuality.setTextColor(ContextCompat.getColor(requireContext(),R.color._40_quality));
        highQuality.setBackgroundResource(R.drawable.quality_40_bg);
        PDFName=view.findViewById(R.id.txtPdfName);
        PDFSize=view.findViewById(R.id.txtPdfSize);
        PDFPages=view.findViewById(R.id.txtPdfPages);
        compressPDFBtn=view.findViewById(R.id.compress_pdf_btn);
        ActualPdfSize=view.findViewById(R.id.tvCurrentSize);
        EstimatedPdfSize=view.findViewById(R.id.tvEstimatedSize);
        sizeReduction=view.findViewById(R.id.tvSizeReduction);
        compressionProgressbar=view.findViewById(R.id.progressBarCompression);

        //***//
        pdf_previewGroup=view.findViewById(R.id.pdf_preview_cmp_pdf);
        btnRemovePdf=view.findViewById(R.id.btnRemovePdf);
        estimateCard=view.findViewById(R.id.estimate);
        estimateCard.setVisibility(View.GONE);
        //***//
        pdf_preview.setVisibility(View.GONE); // hiding the pdf preview  initially

        lowCardBg.setBackgroundResource(R.drawable.radio_btn_bg_cmp_p);
        lowRadio.setChecked(true);
        lowCard.setElevation(40);

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().finish();
                    }
                }
        );


        lowCard.setOnClickListener(v -> {
            selectCard(0);
            selectedRadio[0]=0;
            estimatedResult();

        });

        mediumCard.setOnClickListener(v -> {
            selectCard(1);
            selectedRadio[0]=1;
            estimatedResult();

        });

        highCard.setOnClickListener(v -> {
            selectCard(2);
            selectedRadio[0]=2;
            estimatedResult();

        });

        back_arrow_compress.setOnClickListener(v -> {
            requireActivity().finish();
        });

        uploadPDF_btn.setOnClickListener(v -> {
            Intent intent=PDF_Picker.pick_pdf();
            pdfLauncher.launch(intent);

        });

        compressPDFBtn.setOnClickListener(v -> {

            if (list.size() == 0) {
                Toast.makeText(requireContext(), "Select PDF", Toast.LENGTH_SHORT).show();
                return;}

            listener.onCompressBtnClick(list.get(0).getUri(),selectedRadio[0]);

        });

        pdf_previewGroup.setOnClickListener(v -> {
            Intent intent=new Intent(requireContext(), PdfViewerActivity.class);
            intent.putExtra("pdf_uri",list.get(0).getUri());
            intent.putExtra("mode","view");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            requireContext().startActivity(intent);

        });

        btnRemovePdf.setOnClickListener(v -> {
            list.clear();
            updateUI();
        });


        pdfPicker();



    }

    private void selectCard(int selected) {

        // Reset all cards
        lowCardBg.setBackgroundResource(R.drawable.radio_btn_un_cmp_p);
        mediumCardBg.setBackgroundResource(R.drawable.radio_btn_un_cmp_p);
        highCardBg.setBackgroundResource(R.drawable.radio_btn_un_cmp_p);
        lowCard.setElevation(0);
        mediumCard.setElevation(0);
        highCard.setElevation(0);

        lowRadio.setChecked(false);
        mediumRadio.setChecked(false);
        highRadio.setChecked(false);


        switch (selected) {
            case 0:
                lowCardBg.setBackgroundResource(R.drawable.radio_btn_bg_cmp_p);
                lowRadio.setChecked(true);
                lowCard.setElevation(40);
                break;

            case 1:
                mediumCardBg.setBackgroundResource(R.drawable.radio_btn_bg_cmp_p);
                mediumRadio.setChecked(true);
                mediumCard.setElevation(40);
                break;

            case 2:
                highCardBg.setBackgroundResource(R.drawable.radio_btn_bg_cmp_p);
                highRadio.setChecked(true);
                highCard.setElevation(40);
                break;
        }
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
                                updateUI();


                            }

                        }

                    }

            );
        }
    private void updateUI(){

        if (list.size()!=0) {
            estimateCard.setVisibility(View.VISIBLE);
            long pdfSize=list.get(0).getFileSize();
            String pdf_size=Byte_MB.convertByte(pdfSize);
            PDFName.setText(list.get(0).getName());
            PDFPages.setText(list.get(0).getPageCount() + " Pages");
            PDFSize.setText(pdf_size);
            uploadPDF.setVisibility(View.GONE);
            pdf_preview.setVisibility(View.VISIBLE);
            estimatedResult();

        }
        else {

            uploadPDF.setVisibility(View.VISIBLE);
            pdf_preview.setVisibility(View.GONE);
            estimatedResult();
        }


    }
    private  void estimatedResult(){

        if (list.size()!=0) {

            long pdfSize = list.get(0).getFileSize();
            String pdf_size = Byte_MB.convertByte(pdfSize);
            ActualPdfSize.setText(pdf_size);


            int selectedRadioBtn = selectedRadio[0];
            if (selectedRadioBtn == 0) {
                long estSize = pdfSize * 9 / 10;
                EstimatedPdfSize.setText(Byte_MB.convertByte(estSize));
                sizeReduction.setText(R.string._10);
                compressionProgressbar.setProgress(10);


            } else if (selectedRadioBtn == 1) {
                long estSize = pdfSize * 7 / 10;
                EstimatedPdfSize.setText(Byte_MB.convertByte(estSize));
                sizeReduction.setText(R.string._30);
                compressionProgressbar.setProgress(30);

            } else {
                long estSize = pdfSize * 4 / 10;
                EstimatedPdfSize.setText(Byte_MB.convertByte(estSize));
                sizeReduction.setText(R.string._60);
                compressionProgressbar.setProgress(60);

            }
        }

        else{
            estimateCard.setVisibility(View.GONE);
        }
    }


    public interface onCompress{
        void onCompressBtnClick(Uri uri,int Quality);
    }

}