package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.IOException;

public class GenerateBitmapFromImgUri {

    public static Bitmap generateBitmapFromUri(Context context, Uri imageUri, float scale)
            throws IOException {

        if (context == null)
            throw new IllegalArgumentException("Context cannot be null");

        if (imageUri == null)
            throw new IllegalArgumentException("Image Uri cannot be null");

        if (scale <= 0)
            throw new IllegalArgumentException("Scale must be greater than 0");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            ImageDecoder.Source source =
                    ImageDecoder.createSource(context.getContentResolver(), imageUri);

            return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {

                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                decoder.setMutableRequired(true);

                int originalWidth = info.getSize().getWidth();
                int originalHeight = info.getSize().getHeight();

                int targetWidth = Math.max(1, Math.round(originalWidth * scale));
                int targetHeight = Math.max(1, Math.round(originalHeight * scale));

                decoder.setTargetSize(targetWidth, targetHeight);

            });

        } else {

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(),
                    imageUri
            );

            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            int targetWidth = Math.max(1, Math.round(bitmap.getWidth() * scale));
            int targetHeight = Math.max(1, Math.round(bitmap.getHeight() * scale));

            return Bitmap.createScaledBitmap(
                    bitmap,
                    targetWidth,
                    targetHeight,
                    true
            );
        }
    }
}