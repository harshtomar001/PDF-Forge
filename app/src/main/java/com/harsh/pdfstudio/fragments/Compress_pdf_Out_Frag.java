package com.harsh.pdfstudio.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GenerateName;
import com.harsh.pdfstudio.utils.GetFirstPage;
import com.harsh.pdfstudio.utils.Page_Count;
import com.harsh.pdfstudio.utils.SaveFile;
import com.harsh.pdfstudio.utils.ShareFiles;
import org.jspecify.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Compress_pdf_Out_Frag extends Fragment {

    ProgressBar progressBar;
    TextView progressBarTxtPercent;
    TextView mb_savedTxt;
    TextView originalPdfSize;
    TextView compressedPdfSize;
    TextView pdfName;
    TextView pdfPages;
    TextView pdfSize;
    LinearLayout downloadPdf;
    LinearLayout sharePDF;
    MaterialButton cmp_anotherPDF;
    Uri pdfUri = null;
    long originalSize = -1L;
    int quality;
    ExecutorService executorService= Executors.newSingleThreadExecutor();
    Handler handler=new Handler(Looper.getMainLooper());

    FrameLayout loading;

    TextView textloadingBar;

    TextView pdf_successText;

    ImageView successImage;
    FrameLayout progressDiv;
    View un_Cmp_Card;
    LinearLayout equivalentLayout;
    TextView Photos;
    View minStorageSavedCard;
    TextView VideoTime;
    View tipCard;
    ImageView pdfImage;
    LinearLayout pdfPreview;
    List<FileItem> list=new ArrayList<>();



    public Compress_pdf_Out_Frag() {
        super(R.layout.compress_pdf_out_frag);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {

        super.onViewCreated(view, bundle);



        Bundle bun = getArguments();


        progressBar = view.findViewById(R.id.circularProgress);
        progressBarTxtPercent = view.findViewById(R.id.txtProgressPercent);
        mb_savedTxt = view.findViewById(R.id.txtSavedInfo);
        originalPdfSize = view.findViewById(R.id.original_size);
        compressedPdfSize = view.findViewById(R.id.compressed_size);
        pdfName = view.findViewById(R.id.pdf_name_cmp_out_pdf);
        pdfPages = view.findViewById(R.id.txtPdfPages);
        pdfSize=view.findViewById(R.id.txtPdfSize);
        downloadPdf = view.findViewById(R.id.download_compressed_pdf);
        sharePDF = view.findViewById(R.id.sharePDF_compressPDF);
        cmp_anotherPDF = view.findViewById(R.id.cmp_pdf_another);
        progressDiv=view.findViewById(R.id.progress_layout);
        successImage=view.findViewById(R.id.image_success);
        pdf_successText=view.findViewById(R.id.cmp_completed_textView);
        un_Cmp_Card=view.findViewById(R.id.cmp_uncmp_card);
        equivalentLayout=view.findViewById(R.id.equivalent_image_video_layout);
        //
        loading=view.findViewById(R.id.loading_overlay);
        textloadingBar=view.findViewById(R.id.text_LoadingBar);
        //
        Photos=view.findViewById(R.id.txtPhotos);
        VideoTime=view.findViewById(R.id.txtEquivalentVideo);
        minStorageSavedCard=view.findViewById(R.id.minStorage);
        tipCard=view.findViewById(R.id.tipCard);
        TextView tvTip=view.findViewById(R.id.tvTip);
        pdfImage=view.findViewById(R.id.pdf_image);
        pdfPreview=view.findViewById(R.id.pdf_preview_cmp_out_pdf);


        if (bun != null) {

            pdfUri = bun.getParcelable("uri");
            originalSize = Integer.valueOf(bun.getString("original_size"));
            quality=Integer.parseInt(bun.getString("compressionLevel"));
            updateUi();

        }
        downloadPdf.setOnClickListener(v -> {

            handler.post(downloadingRunnable);

            loading.bringToFront();
            loading.setVisibility(View.VISIBLE);


            executorService.execute(() -> {

                SaveFile.savePDF(requireContext(),
                        list.get(0).getUri(),
                        list.get(0).getName(),
                        list.get(0).getPageCount(),
                        Byte_MB.convertByte(list.get(0).getFileSize()));

                handler.post(() -> {
                   loading.setVisibility(View.GONE);


                });
            });

        });


        sharePDF.setOnClickListener(v -> {

            Intent intent= ShareFiles.sharePDF();
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            startActivity(Intent.createChooser(intent, "Share PDF"));

        });

        cmp_anotherPDF.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager().clearBackStack(null);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container ,
                            new Compress_pdf_Inp_Frag())
                    .commit();

        });

//        SpannableStringBuilder tip = new SpannableStringBuilder();
//        tip.append("Tip: ", new StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        tip.append("For better compression, try reducing image quality or removing unused content.");
//        tvTip.setText(tip);

        pdfPreview.setOnClickListener( v -> {
            Intent intent=new Intent(requireContext(), PdfViewerActivity.class);
            intent.putExtra("pdf_uri",list.get(0).getUri());
            intent.putExtra("mode","preview");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            requireContext().startActivity(intent);

        });


    }


    private final String[] downloadMessages = {"Downloading PDF", "Downloading PDF.", "Downloading PDF..", "Downloading PDF..."};

    private int messageIndex = 0;

    private final Runnable downloadingRunnable = new Runnable() {
        @Override
        public void run() {
            textloadingBar.setText(downloadMessages[messageIndex % downloadMessages.length]);
            messageIndex++;

            handler.postDelayed(this, 1000);
        }
    };


    private void updateUi() {

        long cmp_size= File_Size.getFileSize(requireContext(),pdfUri);
        long difference=originalSize-cmp_size;
        String diffSize=Byte_MB.convertByte(difference);
        String cmpSize=Byte_MB.convertByte(cmp_size);
        int pdf_pages= Page_Count.getPdfPageCount(requireContext(),pdfUri);

        String name=GenerateName.generateUniqueName("compressed_");

        list.add(new FileItem(name,pdfUri,cmp_size,pdf_pages));

        int percent = Math.round(((float) difference / originalSize) * 100);

        boolean noReduction=false;

        if (percent<=2){

            noReduction=true;
            successImage.setImageResource(R.drawable.already_optimized);
            mb_savedTxt.setText(R.string.this_document_is_already_highly_compressed_no_significant_size_reduction_was_possible);
            pdf_successText.setText(R.string.pdf_already_optimized);
            progressDiv.setVisibility(View.GONE);
            equivalentLayout.setVisibility(View.GONE);
            un_Cmp_Card.setVisibility(View.VISIBLE);
            if (quality!=2) {
//                tipCard.setVisibility(View.VISIBLE);
                pdf_successText.setText("Minimal Reduction Achieved!");
                mb_savedTxt.setText("This PDF is already optimized for high-quality output. Try a stronger compression level for smaller file sizes.");


            }

        }
        else if(percent<=5) {

            successImage.setImageResource(R.drawable.optimized);
            mb_savedTxt.setText(R.string.minor_size_reduction_achieved_this_document_was_already_partially_optimized);
            pdf_successText.setText(R.string.pdf_optimized);
            progressDiv.setVisibility(View.GONE);

            if (quality!=2) {
//                tipCard.setVisibility(View.VISIBLE);
                pdf_successText.setText("Limited Reduction Achieved!");
                mb_savedTxt.setText("Further size reduction may be possible with Maximum Compression.");


            }


        }
        else {
            mb_savedTxt.setText(String.format("You saved %s (%s%%)",diffSize,percent));


        }

        progressBar.setProgress(percent);
        progressBarTxtPercent.setText(percent+"%");
        originalPdfSize.setText(Byte_MB.convertByte(originalSize));
        compressedPdfSize.setText(cmpSize);

        pdfName.setText(name);
        pdfPages.setText(pdf_pages+" Pages");
        pdfSize.setText(cmpSize);

        // calculating images and videos minute from the saved bytes


        int videoSpan=Byte_MB.totalVideoMinutes(difference);

        if (videoSpan>=1){
            int images=Byte_MB.totalImages(difference);
            Photos.setText(String.format("≈ %d Photos",images));
            VideoTime.setText(String.format("≈ %d Minutes of HD Video",videoSpan));

        }
        else if(!noReduction)  {
            minStorageSavedCard.setVisibility(View.VISIBLE);
            equivalentLayout.setVisibility(View.GONE);

//            if(quality!=2) tipCard.setVisibility(View.VISIBLE);

        }

        // getting the first page of pdf for the preview

        executorService.execute(() -> {

            Bitmap bitmap= GetFirstPage.getFirstPageBitmap(requireContext(),pdfUri);



            handler.post(() -> {
                pdfImage.setImageBitmap(bitmap);
            });

        });



    }
}