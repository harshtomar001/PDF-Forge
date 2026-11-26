package com.harsh.pdfstudio.ui;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.harsh.pdfstudio.fragments.SplitPdf_Inp_Frag;
import com.harsh.pdfstudio.fragments.SplitPdf_Out_Frag;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.creation.Spliting_PDF;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.SharingViewModel_sp_pdf;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplitPDF extends AppCompatActivity {


    ExecutorService executorService= Executors.newSingleThreadExecutor();
    Handler handler=new Handler(Looper.getMainLooper());
    FrameLayout frameLayoutProgressBar;
    TextView progressbarText;
    WebView loader;

    ArrayList<FileItem> list_Of_Multiple_Pdfs;

    boolean multiplePDfs=false;

    String original_pdf_name;



    @Override
    protected void onCreate(Bundle bundle){

        super.onCreate(bundle);

        setContentView(R.layout.parent_page);

        frameLayoutProgressBar=findViewById(R.id.frame_layout_progressBar);
        progressbarText=findViewById(R.id.loadingBar_textview);
        loader=findViewById(R.id.webViewLoadingBar);


        loader.getSettings().setJavaScriptEnabled(true);
        loader.setBackgroundColor(Color.TRANSPARENT);

        loader.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                loader.evaluateJavascript("setOperation('convert');", null);
            }
        });

        loader.loadUrl("file:///android_asset/pdf_loader.html");
        loader.setVerticalScrollBarEnabled(false);
        loader.setHorizontalScrollBarEnabled(false);

        loader.evaluateJavascript(
                "setOperation('convert'); startAnim();",
                null
        );


        SplitPdf_Inp_Frag splitPdfInpFrag=new SplitPdf_Inp_Frag();

        if (bundle==null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container,splitPdfInpFrag)
                    .addToBackStack(null)
                    .commit();



        }


        splitPdfInpFrag.onSplitPDFClick((pdfUri,fromPage,toPage,original_PDF_Name) -> {

            frameLayoutProgressBar.setVisibility(View.VISIBLE);

            executorService.execute(() -> {

                Uri outputUri;

                if(fromPage==Integer.MIN_VALUE && toPage==Integer.MIN_VALUE){
                    outputUri = null;

                    list_Of_Multiple_Pdfs=Spliting_PDF.getIndividualPDFs(this,pdfUri);
                        multiplePDfs=true;

                }
                else {

                    outputUri = Spliting_PDF.getSplitPDF(this, pdfUri, fromPage, toPage);

//                    Log.d("outputUri",outputUri+"");

                    original_pdf_name=original_PDF_Name;

                }


                handler.post(() -> {

                    loader.evaluateJavascript("completeAnim();", null);

                    handler.postDelayed(() ->{

                        frameLayoutProgressBar.setVisibility(View.GONE);
                        moveOn(outputUri,fromPage,toPage);

                    },1600);

                });



            });



        });


    }



    private void moveOn(Uri outputUri , int fromPage,int toPage){

        SplitPdf_Out_Frag splitPdfOutFrag=new SplitPdf_Out_Frag();

        Bundle bundle=new Bundle();


        if (multiplePDfs){

            SharingViewModel_sp_pdf viewModel= new ViewModelProvider(this).get(SharingViewModel_sp_pdf.class);

            viewModel.setFileList(list_Of_Multiple_Pdfs);
            bundle.putBoolean("multiple_Pdfs",true);


        }
        else {

            bundle.putBoolean("multiple_Pdfs",false);
            bundle.putParcelable("Singleton_pdfUri",outputUri);
//            Log.d("nnnsfd",outputUri+"");
            bundle.putString("fromPage",fromPage+"");
            bundle.putString("toPage",toPage+"");
            bundle.putString("originalPdfName",original_pdf_name);



        }

        splitPdfOutFrag.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.Fragment_container,splitPdfOutFrag)
                .addToBackStack(null)
                .commit();


    }

}
