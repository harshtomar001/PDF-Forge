package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCacheUtils {

    private static final String TAG = "FileCacheUtils";

    public static File copyUriToCache(
            Context context,
            Uri uri
    ) throws IOException {

        String fileName =GenerateName.generateUniqueName("input_") ;

        File inputFile = new File(
                context.getCacheDir(),
                fileName
        );

        try (
                InputStream inputStream =
                        context.getContentResolver()
                                .openInputStream(uri);

                OutputStream outputStream =
                        new FileOutputStream(inputFile)
        ) {

            if (inputStream == null) {
                throw new IOException(
                        "Unable to open input URI"
                );
            }

            byte[] buffer = new byte[64 * 1024];

            int bytesRead;

            while ((bytesRead =
                    inputStream.read(buffer)) != -1) {

                outputStream.write(
                        buffer,
                        0,
                        bytesRead
                );
            }

            outputStream.flush();
        }

        return inputFile;
    }
}