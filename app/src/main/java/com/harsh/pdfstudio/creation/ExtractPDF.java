package com.harsh.pdfstudio.creation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import androidx.core.content.FileProvider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ExtractPDF {

    // NOTE: this was already static in your original code. Preserved as-is
    // to avoid changing your public API / call sites. See the "subtle bugs"
    // section in my earlier explanation for why this is worth revisiting
    // later (not changed here since it's out of scope for this request).
    private static volatile PageExtract listener;

    public void setOnPageExtract(PageExtract listener) {
        this.listener = listener;
    }

    /**
     * Original signature — unchanged behavior. Defaults to 2 worker
     * threads and full-resolution rendering (scale = 1.0f), exactly as
     * before. Existing call sites are unaffected.
     */
    public static ArrayList<Uri> extractImages(
            Context context,
            Uri pdfUri,
            int first,
            int last,
            String format,
            String quality
    ) throws IOException {
        return extractImages(context, pdfUri, first, last, format, quality, 2, 1.0f);
    }

    /**
     * Same as above but with an explicit worker-thread count, for A/B
     * benchmarking (e.g. threadCount=1 to compare against the original
     * single-threaded behavior).
     */
    public static ArrayList<Uri> extractImages(
            Context context,
            Uri pdfUri,
            int first,
            int last,
            String format,
            String quality,
            int threadCount
    ) throws IOException {
        return extractImages(context, pdfUri, first, last, format, quality, threadCount, 1.0f);
    }

    /**
     * Full control overload: threadCount for A/B benchmarking, and
     * renderScale to render directly at a reduced resolution instead of
     * rendering full-res and relying on anything downstream to shrink it.
     *
     * renderScale:
     *   1.0f  = full PDF-native resolution (original behavior, unchanged)
     *   0.6f  = ~36% of the pixels of 1.0f -> roughly proportional cut in
     *           both render() and compress() time, at reduced image
     *           sharpness. Tune this against your own quality bar; there's
     *           no universally "correct" value, it's a tradeoff you choose.
     *
     * Rendering is done via a Matrix passed into PdfRenderer.Page.render(),
     * so pdfium rasterizes directly at the target size — this is not a
     * full-res render followed by a software downscale.
     */
    public static ArrayList<Uri> extractImages(
            Context context,
            Uri pdfUri,
            int first,
            int last,
            String format,
            String quality,
            int threadCount,
            float renderScale
    ) throws IOException {

        if (threadCount < 1) {
            threadCount = 1;
        }
        if (renderScale <= 0f || renderScale > 1f) {
            renderScale = 1.0f;
        }

        final float scale = renderScale;
        final Context appContext = context.getApplicationContext();
        final int totalPages = last - first + 1;

        final Bitmap.CompressFormat type;
        final String actualFormat;

        switch (format) {
            case "PNG":
                type = Bitmap.CompressFormat.PNG;
                actualFormat = ".png";
                break;
            case "WEBP":
                type = Bitmap.CompressFormat.WEBP;
                actualFormat = ".webp";
                break;
            default:
                type = Bitmap.CompressFormat.JPEG;
                actualFormat = ".jpg";
                break;
        }

        final int qual;

        switch (quality) {
            case "High":
                qual = 100;
                break;
            case "Medium":
                qual = 85;
                break;
            case "Low":
                qual = 70;
                break;
            default:
                qual = 90;
                break;
        }

        // Results indexed by (pageNumber - first). Each slot is written by
        // exactly one worker, so no synchronization is needed on the array
        // itself — there is no shared-index race.
        final Uri[] results = new Uri[totalPages];
        final File[] outputFiles = new File[totalPages];

        final AtomicInteger nextPage = new AtomicInteger(first);
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        final AtomicReference<Throwable> firstError = new AtomicReference<>();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>(threadCount);

        for (int w = 0; w < threadCount; w++) {
            futures.add(pool.submit(() -> runWorker(
                    appContext, pdfUri, first, last, type, actualFormat, qual, scale,
                    nextPage, cancelled, firstError, results, outputFiles
            )));
        }

        pool.shutdown();

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                firstError.compareAndSet(null, e);
            } catch (ExecutionException e) {
                firstError.compareAndSet(null, e.getCause() != null ? e.getCause() : e);
            }
        }

        if (firstError.get() != null) {
            // Don't leave partially created files behind on failure.
            for (File f : outputFiles) {
                if (f != null && f.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
            }

            Throwable err = firstError.get();
            if (err instanceof IOException) {
                throw (IOException) err;
            }
            // Preserve your existing "throws IOException" contract for
            // anything else (RuntimeException, OutOfMemoryError, etc.)
            // so the Activity's existing catch block keeps working
            // unchanged.
            throw new IOException("PDF extraction failed", err);
        }

        ArrayList<Uri> list = new ArrayList<>(totalPages);
        for (Uri u : results) {
            if (u != null) {
                list.add(u);
            }
        }
        return list;
    }

    /**
     * Runs on one of the pool threads. Owns its own PFD + PdfRenderer for
     * its entire lifetime (opened once, reused for every page it pulls —
     * not reopened per page), and its own Page/Bitmap/OutputStream per
     * page, all closed/recycled before moving to the next page.
     */
    private static void runWorker(
            Context appContext,
            Uri pdfUri,
            int first,
            int last,
            Bitmap.CompressFormat type,
            String actualFormat,
            int qual,
            float scale,
            AtomicInteger nextPage,
            AtomicBoolean cancelled,
            AtomicReference<Throwable> firstError,
            Uri[] results,
            File[] outputFiles
    ) {
        ParcelFileDescriptor pfd = null;
        PdfRenderer renderer = null;

        try {
            pfd = appContext.getContentResolver().openFileDescriptor(pdfUri, "r");
            if (pfd == null) {
                return;
            }

            renderer = new PdfRenderer(pfd);

            while (!cancelled.get()) {

                int pageNum = nextPage.getAndIncrement();
                if (pageNum > last) {
                    break;
                }

                PdfRenderer.Page page = null;
                Bitmap bitmap = null;
                OutputStream outputStream = null;

                try {
                    page = renderer.openPage(pageNum - 1);

                    // Render directly at the target resolution instead of
                    // full-res + downscale: pdfium rasterizes into a bitmap
                    // sized for the scale we actually want, so both render()
                    // and compress() do proportionally less work when
                    // scale < 1.0f. scale == 1.0f reproduces the original
                    // full-resolution behavior exactly (identity matrix).
                    int targetW = Math.max(1, Math.round(page.getWidth() * scale));
                    int targetH = Math.max(1, Math.round(page.getHeight() * scale));

                    // ARGB_8888 required: PdfRenderer.Page.render() throws
                    // IllegalArgumentException ("Unsupported pixel format")
                    // for RGB_565 / ARGB_4444. Do not change this.
                    bitmap = Bitmap.createBitmap(
                            targetW,
                            targetH,
                            Bitmap.Config.ARGB_8888
                    );

                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);

                    Matrix matrix = null;
                    if (scale != 1.0f) {
                        matrix = new Matrix();
                        matrix.setScale(scale, scale);
                    }

                    page.render(
                            bitmap,
                            null,
                            matrix,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    );

                    // Deterministic, collision-free even with multiple
                    // threads writing at nearly the same instant: page
                    // number is unique per task, nanoTime adds
                    // sub-millisecond uniqueness on top.
                    File outputFile = new File(
                            appContext.getCacheDir(),
                            pageNum + "_" + System.nanoTime() + actualFormat
                    );

                    // Buffered to avoid one syscall per internal write
                    // during compress() — free win, no quality tradeoff.
                    outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                    bitmap.compress(type, qual, outputStream);
                    outputStream.flush();

                    Uri uri = FileProvider.getUriForFile(
                            appContext,
                            appContext.getPackageName() + ".provider",
                            outputFile
                    );

                    int idx = pageNum - first;
                    outputFiles[idx] = outputFile;
                    results[idx] = uri;

                    // Fires once per successfully completed page, from
                    // whichever worker thread finished it. Your existing
                    // listener already does handler.post(...) internally,
                    // and AtomicInteger.incrementAndGet() is thread-safe,
                    // so this is safe to call concurrently from multiple
                    // workers without any change on your side.
                    PageExtract l = listener;
                    if (l != null) {
                        l.pageExtracted();
                    }

                } catch (IOException | RuntimeException e) {
                    firstError.compareAndSet(null, e);
                    cancelled.set(true);
                    break;
                } catch (OutOfMemoryError oom) {
                    firstError.compareAndSet(null, oom);
                    cancelled.set(true);
                    break;
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException ignored) {
                        }
                    }
                    if (page != null) {
                        page.close();
                    }
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }

        } catch (IOException e) {
            firstError.compareAndSet(null, e);
            cancelled.set(true);
        } finally {
            if (renderer != null) {
                renderer.close();
            }
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public interface PageExtract {
        void pageExtracted();
    }
}