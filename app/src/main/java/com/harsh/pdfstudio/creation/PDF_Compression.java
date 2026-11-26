package com.harsh.pdfstudio.creation;

import android.content.Context;
import com.harsh.pdfstudio.model.FileItem;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.net.Uri;
import android.util.Log;


public class PDF_Compression {

     public enum CompressMode { NORMAL, LARGE, EXTREME }

     public static File compressPdf(Context context, ArrayList<FileItem> pdfList, String outputFileName, CompressMode mode) throws IOException {

        Uri  pdfUri=pdfList.get(0).getUri();

        File inputFile = copyUriToTempFile(context, pdfUri);

        File outputFile = new File(context.getCacheDir(), "cmp_" + outputFileName);


        try (PDDocument document = PDDocument.load(inputFile)) {

            document.setAllSecurityToBeRemoved(true);

            if (mode == CompressMode.LARGE) {
                compressImages(document, 0.65f, false);
            } else if (mode == CompressMode.EXTREME) {
                compressImages(document, 0.45f, true);

            }

            document.save(outputFile);
        }


        return outputFile.length()>inputFile.length()?inputFile:outputFile;
    }

     private static File copyUriToTempFile(Context context, Uri uri) throws IOException {

        File tempFile = new File(context.getCacheDir(), "temp_input_" + System.currentTimeMillis());

        try (
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(tempFile)
        )
        {
            if (inputStream == null) {
                throw new IOException("Cannot open input PDF");
            }

            byte[] buffer = new byte[8192];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }

        return tempFile;
    }

     private static void compressImages(PDDocument document, float quality, boolean resize) {



        for (PDPage page : document.getPages()) {

            PDResources resources = page.getResources();
            if (resources == null) continue;

            for (COSName name : resources.getXObjectNames()) {

                try {
                    PDXObject xObject = resources.getXObject(name);

                    if (!(xObject instanceof PDImageXObject)) {
                        continue;
                    }

                    PDImageXObject image = (PDImageXObject) xObject;

//                    Log.d("PDF", String.valueOf(image.getCOSObject().getDictionaryObject(COSName.FILTER)));

//                    Log.d("PDF", "bits=" + image.getBitsPerComponent() + " color=" + image.getColorSpace().getName() + " size=" + image.getWidth() + "x" + image.getHeight());

                    if (canCompress(image)){

//                        Log.d("image","compressing");

                        PDImageXObject compressed_image =compressSingleImage(image,document,quality,resize);

//                        Log.d("PDF",image.getCOSObject().toString());

                        if (compressed_image!=image) {
//                            Log.d("PDF",image.getCOSObject().toString());

                            InputStream is = image.createInputStream();

                            byte[] data = IOUtils.toByteArray(is);

                            long originalSize = data.length;

                            InputStream outputStream = compressed_image.createInputStream();

                            byte[] dat = IOUtils.toByteArray(outputStream);

                            long Size = dat.length;

//                            Log.d("image sizze comparison",""+originalSize +"|||||"+Size);


                            if (Size<originalSize){

                            resources.put(name, compressed_image);}
                        }
                    }



                } catch (Exception e) {
                    Log.e("PDF_COMPRESS", "Image skipped, original kept: " + name, e);
                }
            }
        }



    }

     private static Bitmap resizeImage(Bitmap originalBitmap, float scale) {

        int newWidth = (int) (originalBitmap.getWidth() * scale);
        int newHeight = (int) (originalBitmap.getHeight() * scale);


        if (!(newWidth >0) || !(newHeight >0)) {

            return originalBitmap;
        }


        return Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
        );
    }

     private static  boolean canCompress(PDImageXObject image) {

        try {

            if (image.getBitsPerComponent() != 8) return false;


            if (image.getMask() != null) return false;


            if (image.getSoftMask() != null) return false;


            int width = image.getWidth();

            int height = image.getHeight();

            long pixels = (long) width * height;

            if (pixels > 8_000_000) return false;



//            Object cs = image.getCOSObject().getDictionaryObject(COSName.COLORSPACE);
//            if (cs == null) return false;
//
//            String colorSpace = cs.toString();

//            if (!colorSpace.contains("DeviceRGB") && !colorSpace.contains("DeviceGray")) return false;



            return true;

        } catch (Exception e) {
            return false;
        }
    }

     private static PDImageXObject compressSingleImage(PDImageXObject image, PDDocument document,float quality,boolean resize) throws IOException {

//        Log.d("before","image.getimage");

         Bitmap bitmap = image.getImage();

//         Log.d("after","image.getimage");

         Bitmap finalBitmap = bitmap;

         if (bitmap==null) return image;

         if (resize) finalBitmap = resizeImage(bitmap, 0.6f);

         try {

             PDImageXObject compressedImage = JPEGFactory.createFromImage(document, finalBitmap, quality);

             return compressedImage;
         }
         catch (Exception e){

             return image;
         }
         finally {
             cleanBitmap(bitmap,finalBitmap);
         }




    }

     private  static  void cleanBitmap(Bitmap bitmap,Bitmap finalBitmap){

        if (finalBitmap != bitmap) {
            finalBitmap.recycle();
        }



    }

}