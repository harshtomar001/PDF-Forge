package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FileViewerActivity extends AppCompatActivity {



    @Override

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Uri fileUri = getIntent().getParcelableExtra("file_uri");

        if (fileUri == null) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String mimeType = getContentResolver().getType(fileUri);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_LONG).show();
        }


    }
}
