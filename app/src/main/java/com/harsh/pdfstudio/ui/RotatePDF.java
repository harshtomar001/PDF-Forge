package com.harsh.pdfstudio.ui;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.harsh.pdfstudio.fragments.InputFrag_Rotate_pdf;
import com.harsh.pdfstudio.fragments.Output_Frag_Rotate_pdf;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.creation.RotatingPDF;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.Page_Count;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RotatePDF extends FontBaseActivity {

    private FrameLayout frameProgress;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final InputFrag_Rotate_pdf inputFragment =
            new InputFrag_Rotate_pdf();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.rotate_pdf_main);

        frameProgress = findViewById(
                R.id.frame_layout_progressBar
        );


        if (savedInstanceState == null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.Fragment_container,
                            inputFragment
                    )
                    .commit();
        }


        inputFragment.setOnRotatePDF(list -> {

            if (list == null || list.isEmpty()) {
                return;
            }

            FileItem inputItem = list.get(0);

            startLoading();

            executor.execute(() -> {

                try {

                    Uri outputUri =
                            RotatingPDF.rotatePDF(
                                    RotatePDF.this,
                                    inputItem.getUri()
                            );


                    if (outputUri == null) {

                        showConversionError(
                                "Cannot rotate PDF"
                        );

                        return;
                    }


                    String pdfName =
                            GetFileName.getFileName(
                                    RotatePDF.this,
                                    outputUri
                            );

                    long pdfSize =
                            File_Size.getFileSize(
                                    RotatePDF.this,
                                    outputUri
                            );

                    int pdfPages =
                            Page_Count.getPdfPageCount(
                                    RotatePDF.this,
                                    outputUri
                            );


                    FileItem outputItem =
                            new FileItem(
                                    pdfName,
                                    outputUri,
                                    pdfSize,
                                    pdfPages
                            );


                    handler.post(() -> {

                        stopLoading();

                        sendData(outputItem);

                    });


                } catch (Exception e) {

                    Log.e(
                            "ROTATE_PDF",
                            "PDF rotation failed",
                            e
                    );

                    showConversionError(
                            e.getMessage()
                    );
                }

            });

        });

    }


    private void startLoading() {

        frameProgress.bringToFront();

        frameProgress.setVisibility(
                View.VISIBLE
        );
    }


    private void stopLoading() {

        frameProgress.setVisibility(
                View.GONE
        );
    }


    private void showConversionError(
            String message
    ) {

        handler.post(() -> {

            stopLoading();

            conversionFailed();

            Toast.makeText(
                    RotatePDF.this,
                    message == null
                            ? "Cannot rotate PDF"
                            : message,
                    Toast.LENGTH_SHORT
            ).show();

        });
    }


    private void sendData(FileItem pdfItem) {

        Output_Frag_Rotate_pdf outputFragment =
                new Output_Frag_Rotate_pdf();


        Bundle bundle = new Bundle();

        bundle.putParcelable(
                "pdf_uri",
                pdfItem.getUri()
        );

        bundle.putString(
                "pdfName",
                pdfItem.getName()
        );

        bundle.putString(
                "pdfPages",
                pdfItem.getPageCount() + " Pages"
        );

        bundle.putString(
                "pdfSize",
                Byte_MB.convertByte(
                        pdfItem.getFileSize()
                )
        );


        outputFragment.setArguments(bundle);


        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.Fragment_container,
                        outputFragment
                )
                .addToBackStack(null)
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
                .addToBackStack(null)
                .commit();
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdownNow();
    }
}