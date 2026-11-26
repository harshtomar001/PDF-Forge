package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveBitmapGetUri {

    public static Uri getBitmapUri(Context context, Bitmap bitmap) throws IOException {


        File outputFile=new File(context.getCacheDir(),System.currentTimeMillis()+"jpg");

        FileOutputStream out=new FileOutputStream(outputFile);

        bitmap.compress(Bitmap.CompressFormat.JPEG,90,out);

        out.flush();
        out.close();

        Uri uri= FileProvider.getUriForFile(context,context.getPackageName()+".provider",outputFile);

        return uri;

    }


}
