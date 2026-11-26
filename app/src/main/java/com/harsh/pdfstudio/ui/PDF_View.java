package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class PDF_View extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent incomingIntent = getIntent();
        Uri pdfUri = incomingIntent.getData();

        if (pdfUri == null) {
            finish();
            return;
        }

        Intent intent = new Intent(PDF_View.this, PdfViewerActivity.class);

        intent.putExtra("pdf_uri",pdfUri);
        intent.putExtra("mode","");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
        finish();
    }
}