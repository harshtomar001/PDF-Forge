package com.harsh.pdfstudio.creation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;




import android.content.ContentResolver;

import com.harsh.pdfstudio.utils.NativePngEncoder;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ImageConversion {

    private static onImageConverted listener;

    public void setOnImageConverted(onImageConverted listener){

        this.listener = listener;

    }

    public static ArrayList<Uri> imageConversion(
            Context context,
            ArrayList<Uri> list,
            String conversionType,
            int quality
    ) {

        try {

            return imageConversion(
                    context,
                    list,
                    conversionType,
                    quality,
                    2
            );


        }

        catch (IOException e) {

            return new ArrayList<>();
        }
    }


    public static ArrayList<Uri> imageConversion(
            Context context,
            List<Uri> list,
            String conversionType,
            int quality,
            int threadCount
    ) throws IOException {

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        threadCount = Math.max(1, threadCount);


        threadCount = Math.min(threadCount, 2);


        int cpuThreads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);

        long maxMemory = Runtime.getRuntime().maxMemory();

        int memoryBudgetThreads = (int) (maxMemory / (60L * 1024 * 1024)); // assume ~60MB worst-case per image

        threadCount = Math.min(cpuThreads, Math.max(2, memoryBudgetThreads));

        final Context appContext = context.getApplicationContext();

        final int total = list.size();

        String type = conversionType == null
                ? "jpeg"
                : conversionType.toLowerCase(Locale.ROOT);


        final Bitmap.CompressFormat format;
        final String extension;

        switch (type) {

            case "png":

                format = Bitmap.CompressFormat.PNG;
                extension = "png";

                break;

            case "webp":

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    format = Bitmap.CompressFormat.WEBP_LOSSY;

                }

                else {

                    format = Bitmap.CompressFormat.WEBP;

                }

                extension = "webp";

                break;

            case "jpg":
            case "jpeg":
            default:

                format = Bitmap.CompressFormat.JPEG;
                extension = "jpg";

                break;
        }


        File externalDir = appContext.getExternalFilesDir(null);

        if (externalDir == null) {

            throw new IOException("External files directory unavailable");
        }


        File outputDir = new File(
                externalDir,
                "ConvertedImages"
        );


        if (!outputDir.exists()) {

            if (!outputDir.mkdirs() && !outputDir.exists()) {

                throw new IOException(
                        "Could not create output directory: " + outputDir
                );
            }
        }


        final Uri[] sources = list.toArray(new Uri[0]);

        final Uri[] results = new Uri[total];

        final File[] outputFiles = new File[total];


        final AtomicInteger nextIndex =
                new AtomicInteger(0);

        final AtomicBoolean cancelled =
                new AtomicBoolean(false);

        final AtomicReference<Throwable> firstError =
                new AtomicReference<>();


        ExecutorService pool =
                Executors.newFixedThreadPool(threadCount);


        List<Future<?>> futures =
                new ArrayList<>(threadCount);


        for (int i = 0; i < threadCount; i++) {

            futures.add(
                    pool.submit(() ->
                            runWorker(
                                    appContext,
                                    sources,
                                    outputDir,
                                    format,
                                    extension,
                                    quality,
                                    nextIndex,
                                    cancelled,
                                    firstError,
                                    results,
                                    outputFiles
                            )
                    )
            );
        }


        pool.shutdown();


        for (Future<?> future : futures) {

            try {

                future.get();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                firstError.compareAndSet(
                        null,
                        e
                );

                cancelled.set(true);

            }
            catch (ExecutionException e) {

                Throwable cause = e.getCause();

                firstError.compareAndSet(
                        null,
                        cause != null ? cause : e
                );

                cancelled.set(true);
            }
        }


        if (firstError.get() != null) {

            cleanupFiles(outputFiles);

            Throwable error = firstError.get();

            if (error instanceof IOException) {
                throw (IOException) error;
            }

            throw new IOException(
                    "Image conversion failed",
                    error
            );
        }


        ArrayList<Uri> outputList = new ArrayList<>(total);


        for (Uri uri : results) {

            if (uri != null) {
                outputList.add(uri);
            }
        }


        return outputList;
    }


    private static void runWorker(
            Context appContext,
            Uri[] sources,
            File outputDir,
            Bitmap.CompressFormat format,
            String extension,
            int quality,
            AtomicInteger nextIndex,
            AtomicBoolean cancelled,
            AtomicReference<Throwable> firstError,
            Uri[] results,
            File[] outputFiles
    ) {

        ContentResolver resolver =
                appContext.getContentResolver();

        while (!cancelled.get()) {

            int index = nextIndex.getAndIncrement();

            if (index >= sources.length) {
                break;
            }

            Uri imageUri = sources[index];

            Bitmap bitmap = null;
            File outputFile = null;

            try {

                int rotation = getRotation(
                        resolver,
                        imageUri
                );

                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inScaled = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                try (InputStream input = resolver.openInputStream(imageUri)) {

                    if (input == null) {

                        throw new IOException(
                                "Could not open URI: " +
                                        imageUri
                        );
                    }

                    bitmap = BitmapFactory.decodeStream(
                            input,
                            null,
                            options
                    );
                }


                if (bitmap == null) {

                    throw new IOException(
                            "Could not decode image: " +
                                    imageUri
                    );
                }


                if (rotation != 0) {

                    Matrix matrix = new Matrix();

                    matrix.postRotate(rotation);

                    Bitmap rotatedBitmap =
                            Bitmap.createBitmap(
                                    bitmap,
                                    0,
                                    0,
                                    bitmap.getWidth(),
                                    bitmap.getHeight(),
                                    matrix,
                                    true
                            );

                    /*
                     * createBitmap() can sometimes return
                     * the original bitmap.
                     *
                     * Only recycle when a new bitmap was created.
                     */

                    if (rotatedBitmap != bitmap) {

                        bitmap.recycle();

                        bitmap = rotatedBitmap;
                    }
                }


                outputFile = new File(
                                outputDir,
                                "converted_" +
                                        index +
                                        "_" +
                                        System.nanoTime() +
                                        "." +
                                        extension
                        );

                boolean success;

                try (OutputStream outputStream =
                             new BufferedOutputStream(
                                     new FileOutputStream(outputFile),
                                     64 * 1024
                             )) {

                    success = bitmap.compress(
                            format,
                            quality,
                            outputStream
                    );

                    outputStream.flush();
                }
                if (!success) {

                    throw new IOException(
                            "Compression failed: " +
                                    imageUri
                    );
                }



                Uri outputUri =
                        FileProvider.getUriForFile(
                                appContext,
                                appContext.getPackageName()
                                        + ".provider",
                                outputFile
                        );


                outputFiles[index] = outputFile;

                results[index] = outputUri;




                listener.onImageConvertedSuccess();



                if (bitmap != null &&
                        !bitmap.isRecycled()) {

                    bitmap.recycle();

                    bitmap = null;
                }


            }

            catch (IOException | RuntimeException e) {

                firstError.compareAndSet(
                        null,
                        e
                );

                cancelled.set(true);


                // Delete incomplete output file

                if (outputFile != null &&
                        outputFile.exists()) {

                    //noinspection ResultOfMethodCallIgnored
                    outputFile.delete();
                }

            }

            catch (OutOfMemoryError oom) {

                Log.e(
                        "CONVERT",
                        "Out of memory while processing: " +
                                imageUri,
                        oom
                );


                firstError.compareAndSet(
                        null,
                        oom
                );

                cancelled.set(true);
            }

            finally {

                /*
                 * Safety cleanup.
                 *
                 * This guarantees that the bitmap is released
                 * even when an exception occurs.
                 */

                if (bitmap != null &&
                        !bitmap.isRecycled()) {

                    bitmap.recycle();

                    bitmap = null;
                }
            }
        }
    }


    private static int getRotation(
            ContentResolver resolver,
            Uri uri
    ) {

        try (InputStream input = resolver.openInputStream(uri)) {

            if (input == null) {
                return 0;
            }


            ExifInterface exif = new ExifInterface(input);


            int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
            );


            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;

                default:
                    return 0;
            }


        }
        catch (Exception e) {

            Log.w(
                    "CONVERT",
                    "Could not read EXIF: " + uri,
                    e
            );

            return 0;

        }
    }


    private static void cleanupFiles(
            File[] files
    ) {

        for (File file : files) {

            if (file != null && file.exists()) {

                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

    public interface onImageConverted{

        void onImageConvertedSuccess();

    }

}