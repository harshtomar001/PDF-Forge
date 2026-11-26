package com.harsh.pdfstudio.utils;

import android.graphics.Bitmap;

public final class NativePngEncoder {

    static {
        System.loadLibrary("pngencoder");
    }

    private NativePngEncoder() {
        // Utility class
    }

    /**
     * Encodes a Bitmap as PNG using the native libspng encoder.
     *
     * @param bitmap Bitmap to encode
     * @param compressionLevel 1 (fastest) to 9 (smallest)
     * @param hasAlpha whether the PNG should contain an alpha channel
     * @return PNG bytes, or null if native encoding fails
     */
    public static native byte[] encode(
            Bitmap bitmap,
            int compressionLevel,
            boolean hasAlpha
    );
}