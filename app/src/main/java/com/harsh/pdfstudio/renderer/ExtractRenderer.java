package com.harsh.pdfstudio.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import com.harsh.pdfstudio.utils.WhiteBG;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExtractRenderer {


    public static Uri saveBitmapAsPng(
            Context context,
            Bitmap bitmap,String fileName) {

        try {

            File dir = new File(
                    context.getCacheDir(),
                    context.getPackageName()
            );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(
                    dir,
                    ( fileName.isEmpty() ?  System.nanoTime():fileName )+ ".png"
            );

            try (FileOutputStream out = new FileOutputStream(file)) {


                bitmap = WhiteBG.addWhiteBackground(bitmap);



                boolean success = bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,   // ignored for PNG
                        out
                );

                if (!success) {
                    return null;
                }

                out.flush();
            }

            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

        } catch (IOException e) {


            Toast.makeText(
                    context,
                    "Something went wrong!",
                    Toast.LENGTH_LONG
            ).show();

            return null;
        }
    }

    public static Uri saveBitmapAsJpg(
            Context context,
            Bitmap bitmap,String fileName
    ) {

        try {

            File dir = new File(
                    context.getCacheDir(),
                    context.getPackageName()
            );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, (fileName.isEmpty() ? System.nanoTime() :fileName) + ".jpg");

            try (FileOutputStream out = new FileOutputStream(file)) {

                bitmap = WhiteBG.addWhiteBackground(bitmap);

                boolean success = bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        out
                );

                if (!success) {
                    return null;
                }

                out.flush();
            }

            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

        } catch (IOException e) {


            Toast.makeText(
                    context,
                    "Something went wrong!",
                    Toast.LENGTH_LONG
            ).show();

            return null;
        }
    }

    public static Uri exportBitmapToPdf(
            Context context,
            Bitmap bitmap,String  fileName) {

        try {

            File dir = new File(context.getCacheDir(), context.getPackageName());

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File outputFile = new File(dir, String.valueOf(( fileName.isEmpty() ? System.nanoTime() : fileName)));

            try (OutputStream outputStream = new FileOutputStream(outputFile);
                 PDDocument document = new PDDocument()) {

                PDPage page = new PDPage(
                        new PDRectangle(
                                bitmap.getWidth(),
                                bitmap.getHeight()
                        )
                );

                document.addPage(page);

                PDImageXObject image =
                        LosslessFactory.createFromImage(document, bitmap);

                try (PDPageContentStream stream =
                             new PDPageContentStream(document, page)) {

                    stream.drawImage(
                            image,
                            0,
                            0,
                            bitmap.getWidth(),
                            bitmap.getHeight()
                    );
                }

                document.save(outputStream);
            }

            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    outputFile
            );

        }
        catch (IOException e) {

            Toast.makeText(
                    context,
                    "Something went wrong!",
                    Toast.LENGTH_LONG
            ).show();

            return null;
        }
    }


}
