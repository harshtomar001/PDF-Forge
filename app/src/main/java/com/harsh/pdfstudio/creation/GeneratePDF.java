package com.harsh.pdfstudio.creation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import androidx.exifinterface.media.ExifInterface;

import com.tom_roush.pdfbox.io.MemoryUsageSetting;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;


public class GeneratePDF {

    private static final String TAG = "GeneratePDF";
    private static final int MAX_IMAGE_DIMENSION = 2000;
    private static final float JPEG_QUALITY = 0.85f;

    public static File createPdf(Context context, ArrayList<Uri> imageUris, String fileName) throws IOException {

        File pdfFile = new File(context.getCacheDir(), fileName);

        try (PDDocument document = new PDDocument(MemoryUsageSetting.setupTempFileOnly())) {

            for (Uri uri : imageUris) {

                Bitmap bitmap = decodeBitmap(context, uri, MAX_IMAGE_DIMENSION);

                if (bitmap == null) {
                    continue; // bad/unreadable image — skip, don't abort the whole PDF
                }

                if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                    Bitmap converted = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                    bitmap.recycle();
                    bitmap = converted;
                }

                PDPage page = new PDPage(new PDRectangle(bitmap.getWidth(), bitmap.getHeight()));
                document.addPage(page);

                // JPEG instead of lossless PNG: much smaller output, cheaper to encode
                PDImageXObject image = JPEGFactory.createFromImage(document, bitmap, JPEG_QUALITY);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
                }

                bitmap.recycle();
            }

            document.save(pdfFile);
        }

        return pdfFile;
    }

    private static Bitmap decodeBitmap(Context context, Uri uri, int maxDimension) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return decodeWithImageDecoder(context, uri, maxDimension);
            }
            return decodeWithBitmapFactory(context, uri, maxDimension);
        } catch (IOException e) {
            Log.e(TAG, "Failed to decode " + uri, e);
            return null; // caller skips this image instead of crashing the batch
        }
    }

    private static Bitmap decodeWithImageDecoder(Context context, Uri uri, int maxDimension) throws IOException {

        ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);

        return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
            Size size = info.getSize();
            Size target = resizedDimension(size.getWidth(), size.getHeight(), maxDimension);
            decoder.setTargetSize(target.getWidth(), target.getHeight());
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
        });
        // ImageDecoder applies EXIF rotation automatically — no manual matrix needed here
    }

    private static Bitmap decodeWithBitmapFactory(Context context, Uri uri, int maxDimension) throws IOException {

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;

        try (InputStream boundsStream = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(boundsStream, null, bounds);
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension);

        Bitmap bitmap;
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(stream, null, decodeOptions);
        }

        // unlike ImageDecoder, BitmapFactory does NOT auto-rotate — apply EXIF manually
        return applyExifRotation(context, uri, bitmap);
    }

    private static int calculateInSampleSize(int width, int height, int maxDimension) {
        int sampleSize = 1;
        while ((width / sampleSize) > maxDimension || (height / sampleSize) > maxDimension) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    private static Bitmap applyExifRotation(Context context, Uri uri, Bitmap bitmap) {

        if (bitmap == null) return null;

        try (InputStream input = context.getContentResolver().openInputStream(uri)) {

            ExifInterface exif = new ExifInterface(input);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int degrees;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:  degrees = 90;  break;
                case ExifInterface.ORIENTATION_ROTATE_180: degrees = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270: degrees = 270; break;
                default: return bitmap;
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);

            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotated;

        } catch (IOException e) {
            Log.e(TAG, "EXIF read failed for " + uri, e);
            return bitmap;
        }
    }

    private static Size resizedDimension(int width, int height, int maxSize) {

        if (width <= maxSize && height <= maxSize) {
            return new Size(width, height);
        }

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        return new Size(Math.round(width * ratio), Math.round(height * ratio));

    }
}