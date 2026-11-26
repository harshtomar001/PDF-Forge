package com.harsh.pdfstudio.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.harsh.pdfstudio.fragments.Docx_Pdf_Inp_Frag;
import com.harsh.pdfstudio.fragments.Docx_Pdf_Out_Frag;
import com.harsh.pdfstudio.fragments.Pptx_Pdf_Out_Frag;
import com.harsh.pdfstudio.fragments.serverDown;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.utils.ApiClient;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.Page_Count;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class Docx_to_Pdf extends AppCompatActivity {

    FrameLayout progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceBundle){

        super.onCreate(savedInstanceBundle);

        setContentView(R.layout.rotate_pdf_main);

        progressbar=findViewById(R.id.frame_layout_progressBar);

        Docx_Pdf_Inp_Frag docxPdfInpFrag=new Docx_Pdf_Inp_Frag();

        if (savedInstanceBundle==null){

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container,docxPdfInpFrag)
                    .commit();

        }

        docxPdfInpFrag.onPDFCLick((uri , name) -> {
            progressbar.setVisibility(View.VISIBLE);
            uploadDoc(uri,name);

        });

    }

    private  void uploadDoc(Uri uri, String docName) {

        RequestBody fileBody = new RequestBody() {

            @Override
            public MediaType contentType() {
                return MediaType.parse("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {

                InputStream inputStream = getContentResolver().openInputStream(uri);

                byte[] buffer = new byte[8192];
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, read);
                }

                inputStream.close();
            }
        };

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", docName, fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://10.154.164.228:5291/PdfStudio/pptx-to-pdf")
                .post(requestBody)
                .build();

        ApiClient.client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                    progressbar.setVisibility(View.GONE);
                    Log.d("error",e.getMessage());
                    Toast.makeText(Docx_to_Pdf.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    serverDown();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        progressbar.setVisibility(View.GONE);
                        Toast.makeText(Docx_to_Pdf.this, "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                        conversionFailed();
                    });
                    return;
                }
                else if (response==null) {
                    progressbar.setVisibility(View.GONE);
                    Toast.makeText(Docx_to_Pdf.this, "Empty file from server " , Toast.LENGTH_LONG).show();
                    conversionFailed();
                    return;
                }

                String pdfName = docName.substring(0, docName.lastIndexOf('.'));

                InputStream inputStream = response.body().byteStream();

                File pdfFile = new File(getExternalFilesDir(null), pdfName + ".pdf");

                FileOutputStream outputStream = new FileOutputStream(pdfFile);

                byte[] buffer = new byte[8192];
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }

                outputStream.close();
                inputStream.close();

                Uri pdfUri = FileProvider.getUriForFile(
                        Docx_to_Pdf.this,
                        getPackageName() + ".provider",
                        pdfFile
                );

                runOnUiThread(() -> {
                    progressbar.setVisibility(View.GONE);
                    moveON(pdfUri);
                });
            }
        });
    }


    private void moveON(Uri pdfUri){

       Pptx_Pdf_Out_Frag docxPdfOutFrag=new Docx_Pdf_Out_Frag(); // parent ref => child obj

        Bundle bundle=new Bundle();
        bundle.putString("pdfName", GetFileName.getFileName(this,pdfUri));
        bundle.putString("pdfSize", Byte_MB.convertByte(File_Size.getFileSize(this,pdfUri)));
        bundle.putString("pdfPages", Page_Count.getPdfPageCount(this,pdfUri)+" Pages");
        bundle.putParcelable("pdf_uri",pdfUri);
        docxPdfOutFrag.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container,docxPdfOutFrag)
                .addToBackStack(null)
                .commit();

    }

    private void serverDown(){

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container,new serverDown())
                .addToBackStack(null)
                .commit();

    }


    private void conversionFailed(){

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container,new ConversionFailed())
                .addToBackStack(null)
                .commit();
    }

}