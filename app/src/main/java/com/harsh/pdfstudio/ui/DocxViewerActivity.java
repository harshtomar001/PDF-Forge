package com.harsh.pdfstudio.ui;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.harsh.pdfstudio.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DocxViewerActivity extends AppCompatActivity {

    WebView webView;
    String base64Data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.docx_viewer);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false); // hides zoom buttons
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);



        Uri tempUri = getIntent().getParcelableExtra("file_uri");

        if(tempUri == null){
              tempUri = getIntent().getData();
        }
        final  Uri uri=tempUri;
        new Thread(() -> {


            try {

                InputStream inputStream = getContentResolver().openInputStream(uri);

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int nRead;

                while ((nRead = inputStream.read(data)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                byte[] bytes = buffer.toByteArray();

                base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);

                runOnUiThread(() -> webView.reload());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {

                view.evaluateJavascript(
                        "openDocx('" + base64Data + "')",
                        null
                );

            }
        });

        webView.loadUrl("file:///android_asset/docxviewer.html");

    }
}