package com.harsh.pdfstudio.ui;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.harsh.pdfstudio.fragments.InpFrag_ImgFromPDF;
import com.harsh.pdfstudio.fragments.OutFrag_ImgFromPDF;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.creation.ExtractPDF;
import com.harsh.pdfstudio.utils.GetFileName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ImgFromPDF extends AppCompatActivity {

    TextView  countImgExtracted;
    InpFrag_ImgFromPDF inpFragImgFromPDF=new InpFrag_ImgFromPDF();
    ExecutorService service= Executors.newSingleThreadExecutor();
    Handler handler=new Handler(Looper.getMainLooper());

    ProgressBar progressBar ;

    LinearLayout loader_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.rotate_pdf_main); // just using for  parent  layout // actual page handles in fragment


        progressBar = findViewById(R.id.exportProgress);
        loader_layout = findViewById(R.id.loader_layout);
        countImgExtracted=findViewById(R.id.extract_count_text);


        if (savedInstanceState==null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container,inpFragImgFromPDF).commit();
        }


        inpFragImgFromPDF.setOnClickExtractImage(
                (first, last, uri, format, quality) -> {

                    showProgressBar();

                    progressBar.setProgress(0);

                    String pdfName = GetFileName.getFileName(this, uri);

                    int totalPages = last - first + 1;

                    countImgExtracted.setText(
                            "0 / " + totalPages + " pages processed"
                    );


                    ExtractPDF extractPDF = new ExtractPDF();

                    AtomicInteger processedPages = new AtomicInteger(0);



                    extractPDF.setOnPageExtract(() -> {

                        int page = processedPages.incrementAndGet();

                        int progress = (page * 100) / totalPages;


                        handler.post(() -> {

                            if (isFinishing() || isDestroyed()) {
                                return;
                            }

                            progressBar.setProgress(progress);

                            countImgExtracted.setText(
                                    page + " / " +
                                            totalPages +
                                            " pages processed"
                            );

                        });

                    });



                    service.execute(() -> {

                        ArrayList<Uri> list;

                        try {

                            list = extractPDF.extractImages(
                                    getApplicationContext(),
                                    uri,
                                    first,
                                    last,
                                    format,
                                    quality,
                                    1
                            );

                        }

                        catch (IOException e) {

                            handler.post(() -> {

                                hideProgressBar();

                                conversionFailed();

                            });

                            return;

                        }

                        catch (Exception e) {

                            e.printStackTrace();

                            handler.post(() -> {

                                hideProgressBar();

                                conversionFailed();

                            });

                            return;

                        }

                        catch (OutOfMemoryError e) {

                            handler.post(() -> {

                                hideProgressBar();

                                conversionFailed();

                            });

                            return;

                        }


                        // -----------------------------------------
                        // EXTRACTION FINISHED
                        // -----------------------------------------

                        ArrayList<Uri> finalList =
                                new ArrayList<>(list);


                        handler.post(() -> {

                            hideProgressBar();


                            if (finalList.isEmpty()) {

                                conversionFailed();

                                return;
                            }


                            if (format.equals("Original Format")) {

                                openOutputFragment(
                                        finalList,
                                        "JPG",
                                        pdfName
                                );

                            }

                            else {

                                openOutputFragment(
                                        finalList,
                                        format,
                                        pdfName
                                );
                            }

                        });

                    });

                });

    }

    public void  showProgressBar(){


        if (progressBar == null && loader_layout == null) return;


        loader_layout.setVisibility(View.VISIBLE);


    }

    public void hideProgressBar(){

        if (progressBar == null && loader_layout == null) return;

        loader_layout.setVisibility(View.GONE);


    }


    private void  openOutputFragment(ArrayList<Uri> list,String format, String fileName){

        OutFrag_ImgFromPDF outputFragment=new OutFrag_ImgFromPDF();

        ArrayList<String> list1=new ArrayList<>();

        for (Uri uri:list){

            list1.add(uri.toString());

        }


        Bundle bundle=new Bundle();

        bundle.putStringArrayList("list",list1);
        bundle.putString("format",format);
        bundle.putString("fileName",fileName);

        outputFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container,outputFragment)
                .commit();

    }

    private void conversionFailed(){

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container,
                        new ConversionFailed("for_pdf_operation"))
                .addToBackStack(null)
                .commit();
    }


}
