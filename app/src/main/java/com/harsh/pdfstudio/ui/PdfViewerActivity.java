package com.harsh.pdfstudio.ui;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.Page_Count;
import com.harsh.pdfstudio.utils.SaveFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfViewerActivity extends AppCompatActivity {

    ExecutorService executorService= Executors.newSingleThreadExecutor();
    Handler handler=new Handler(Looper.getMainLooper());

    TextView progressText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_viewer);

        PDFView pdfView = findViewById(R.id.pdfView);
        Uri pdfUri = getIntent().getParcelableExtra("pdf_uri");

        MaterialCardView downloadBtn=findViewById(R.id.download_card_btn);
        progressText=findViewById(R.id.text_LoadingBar);

        View loadingBarLayout=findViewById(R.id.loading_overlay);

        if (pdfUri == null) {
            Toast.makeText(this, "Failed to open PDF", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pdfView.fromUri(pdfUri)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .spacing(2)
                .load();

        String mode=getIntent().getStringExtra("mode");

        if (!mode.equals("preview")){
            downloadBtn.setVisibility(View.GONE);
        }

        downloadBtn.setOnClickListener(v -> {
            loadingBarLayout.setVisibility(View.VISIBLE);
            loadingBarLayout.bringToFront();

            progressText.setText("Downloading File...");
            executorService.execute(() -> {


                SaveFile.savePDF(this,
                        pdfUri,
                        GetFileName.getFileName(this,pdfUri),
                        Page_Count.getPdfPageCount(this,pdfUri),
                        Byte_MB.convertByte(File_Size.getFileSize(this,pdfUri)));

                handler.post(() -> {

                    loadingBarLayout.setVisibility(View.GONE);

                });

            });


        });

    }
}




