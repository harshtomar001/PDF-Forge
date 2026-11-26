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
import android.widget.Toast;

import com.harsh.pdfstudio.fragments.InputFrag_Edit_Img;
import com.harsh.pdfstudio.fragments.OutputFrag_Edit_Img;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.creation.ImageConversion;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class EditImage extends FontBaseActivity {

    FrameLayout frame_progress;

    LinearLayout loader_layout;

    ProgressBar progressBar;

    TextView progressBartv , progressBarPageCount;

    ArrayList<Uri> imageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_img_main);

        InputFrag_Edit_Img inputFragment = new InputFrag_Edit_Img();


        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container, inputFragment)
                    .addToBackStack(null)
                    .commit();


        frame_progress = findViewById(R.id.frame_layout_progressBar);  // to show progress  bar

        progressBar = findViewById(R.id.exportProgress);
        loader_layout = findViewById(R.id.loader_layout);
        progressBartv = findViewById(R.id.extract_progress_text);
        progressBarPageCount = findViewById(R.id.extract_count_text);


        inputFragment.setOnGenerateImageClick((imageList,outputType, imageQuality) ->{


            showLoader();

            ExecutorService executor = Executors.newSingleThreadExecutor();

            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {

                int totalPages = imageList.size();

                progressBartv.setText(R.string.extracting_images);

                progressBarPageCount.setText(
                        String.format(getString(R.string._0_d_pages_processed), totalPages)
                );


                ImageConversion imageConversion = new ImageConversion();

                AtomicInteger processedPages = new AtomicInteger(0);



                imageConversion.setOnImageConverted(() -> {

                    int page = processedPages.incrementAndGet();

                    int progress = (page * 100) / totalPages;


                    handler.post(() -> {

                        if (isFinishing() || isDestroyed()) return;


                        progressBar.setProgress(progress);

                        progressBarPageCount.setText(
                                String.format(getString(R.string.d_d_pages_processed), page, totalPages)
                        );

                    });

                });

                imageUris = imageConversion.imageConversion(
                                this,
                                imageList,
                                outputType,
                                imageQuality
                        );


                handler.post(() -> {

                    hideLoader();

                    if (imageUris == null) {

                        Toast.makeText(this, "Something went Wrong!", Toast.LENGTH_SHORT).show();
                        return;

                    }

                    openOutputFragment(imageUris, outputType);

                });

            });


        });

        }

    }


    private void openOutputFragment(ArrayList<Uri> imageUris, String format) {

        OutputFrag_Edit_Img fragment = new OutputFrag_Edit_Img();

        Bundle bundle = new Bundle();
        ArrayList<String> list = new ArrayList<>();

        for (Uri uri : imageUris) {
            list.add(uri.toString());
        }

        bundle.putStringArrayList("images", list);
        bundle.putString("format", format);

        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


    private void showLoader(){

        if (loader_layout != null){

            loader_layout.setVisibility(View.VISIBLE);
            loader_layout.setFocusable(true);
            loader_layout.setClickable(true);
            loader_layout.bringToFront();
            loader_layout.requestFocus();


        }


    }

    private void hideLoader(){

        if (loader_layout != null){

            loader_layout.setVisibility(View.GONE);

        }

    }


}