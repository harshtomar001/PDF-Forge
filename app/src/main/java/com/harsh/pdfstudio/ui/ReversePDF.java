package com.harsh.pdfstudio.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.harsh.pdfstudio.fragments.InputFrag_Rev_pdf;
import com.harsh.pdfstudio.fragments.OutputFrag_Rev_pdf;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.creation.ReversingPDF;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.Page_Count;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReversePDF extends FontBaseActivity {

    private static final String TAG = "ReversePDF";

    private InputFrag_Rev_pdf inputFragRevPdf;

    private FrameLayout progressBar;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private boolean isProcessing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.rotate_pdf_main);

        initializeViews();

        initializeInputFragment();

        setupReverseListener();
    }


    private void initializeViews() {

        progressBar =
                findViewById(
                        R.id.frame_layout_progressBar
                );
    }


    private void initializeInputFragment() {

        if (savedInstanceStateIsEmpty()) {

            inputFragRevPdf =
                    new InputFrag_Rev_pdf();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.Fragment_container,
                            inputFragRevPdf
                    )
                    .commit();

        } else {

            inputFragRevPdf =
                    (InputFrag_Rev_pdf)
                            getSupportFragmentManager()
                                    .findFragmentById(
                                            R.id.Fragment_container
                                    );
        }
    }


    private boolean savedInstanceStateIsEmpty() {

        return getSupportFragmentManager()
                .findFragmentById(
                        R.id.Fragment_container
                ) == null;
    }


    private void setupReverseListener() {

        if (inputFragRevPdf == null) {
            return;
        }


        inputFragRevPdf.setOnReverse(uri -> {

            if (uri == null) {

                Toast.makeText(
                        this,
                        "Invalid PDF",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            if (isProcessing) {

                Log.d(
                        TAG,
                        "Reverse operation already running"
                );

                return;
            }


            reversePdf(uri);

        });
    }


    private void reversePdf(Uri inputUri) {

        isProcessing = true;

        showProgress();


        executor.execute(() -> {

            try {

                Log.d(
                        TAG,
                        "Starting PDF reversal"
                );


                // --------------------------------------------
                // Heavy PDF operation
                // --------------------------------------------

                Uri outputUri =
                        ReversingPDF.reversePDF(
                                this,
                                inputUri
                        );


                // --------------------------------------------
                // Operation failed
                // --------------------------------------------

                if (outputUri == null) {

                    runOnUiThread(() -> {

                        isProcessing = false;

                        hideProgress();

                        conversionFailed();

                        Toast.makeText(
                                this,
                                "Failed to reverse PDF",
                                Toast.LENGTH_SHORT
                        ).show();

                    });

                    return;
                }


                Log.d(
                        TAG,
                        "Output URI: " + outputUri
                );


                // --------------------------------------------
                // Read output information
                // --------------------------------------------

                String pdfName =
                        GetFileName.getFileName(
                                this,
                                outputUri
                        );


                long pdfSize =
                        File_Size.getFileSize(
                                this,
                                outputUri
                        );


                int pdfPages =
                        Page_Count.getPdfPageCount(
                                this,
                                outputUri
                        );


                // --------------------------------------------
                // Create FileItem
                // --------------------------------------------

                FileItem pdfItem =
                        new FileItem(
                                pdfName,
                                outputUri,
                                pdfSize,
                                pdfPages
                        );


                // --------------------------------------------
                // Send result to UI
                // --------------------------------------------

                runOnUiThread(() -> {

                    isProcessing = false;

                    hideProgress();

                    sendData(pdfItem);

                });


            } catch (Exception e) {

                Log.e(
                        TAG,
                        "PDF reversal failed",
                        e
                );


                runOnUiThread(() -> {

                    isProcessing = false;

                    hideProgress();

                    conversionFailed();

                    String message =
                            e.getMessage();

                    if (
                            message == null
                                    || message.isEmpty()
                    ) {

                        message =
                                "Something went wrong";

                    }


                    Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        });
    }



    private void showProgress() {

        progressBar.bringToFront();

        progressBar.setElevation(
                300f
        );

        progressBar.setVisibility(
                View.VISIBLE
        );
    }


    private void hideProgress() {

        progressBar.setVisibility(
                View.GONE
        );
    }



    private void sendData(FileItem pdfItem) {

        if (pdfItem == null) {

            conversionFailed();

            return;
        }


        OutputFrag_Rev_pdf outputFragment = new OutputFrag_Rev_pdf();


        Bundle bundle = new Bundle();


        // PDF URI

        bundle.putParcelable(
                "pdf_uri",
                pdfItem.getUri()
        );


        // PDF name

        bundle.putString(
                "pdfName",
                pdfItem.getName()
        );


        // PDF pages

        bundle.putString(
                "pdfPages",
                pdfItem.getPageCount()
                        + " Pages"
        );


        // PDF size

        bundle.putString(
                "pdfSize",
                Byte_MB.convertByte(
                        pdfItem.getFileSize()
                )
        );


        outputFragment.setArguments(
                bundle
        );


        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.Fragment_container,
                        outputFragment
                )
                .addToBackStack(
                        "reverse_output"
                )
                .commit();

    }



    private void conversionFailed() {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.Fragment_container,
                        new ConversionFailed(
                                "for_pdf_operation"
                        )
                )
                .addToBackStack(
                        "reverse_error"
                )
                .commit();
    }



    @Override
    protected void onDestroy() {

        // Stop background work when Activity is destroyed.

        executor.shutdownNow();

        super.onDestroy();
    }
}