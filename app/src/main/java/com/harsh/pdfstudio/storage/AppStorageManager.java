package com.harsh.pdfstudio.storage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class AppStorageManager {

    private static final String GENERATED_PDFS_DIR = "generated_pdfs";

    public static final long AUTO_CLEANUP_MAX_AGE_MILLIS = TimeUnit.HOURS.toMillis(2);

    private final Context appContext;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AppStorageManager(Context context) {
        this.appContext = context.getApplicationContext();
    }


    public static class StorageBreakdown {
        public final long pdfBytes;
        public final long cacheBytes;
        public final long otherBytes;

        public StorageBreakdown(long pdfBytes, long cacheBytes, long otherBytes) {
            this.pdfBytes = pdfBytes;
            this.cacheBytes = cacheBytes;
            this.otherBytes = otherBytes;
        }

        public long getTotalBytes() {
            return pdfBytes + cacheBytes + otherBytes;
        }

        public float percentageOf(long part) {
            long total = getTotalBytes();
            if (total <= 0L) return 0f;
            return (float) (part * 100.0 / total);
        }

    }

    public interface BreakdownCallback {
        void onResult(StorageBreakdown breakdown);
    }

    public interface ClearCallback {
        void onResult(boolean success);
    }

    public interface CleanupCallback {
        void onResult(int deletedCount);
    }

    private File generatedPdfsDir() {

        File dir = new File(appContext.getFilesDir(), GENERATED_PDFS_DIR);

        if (!dir.exists()) {

            dir.mkdirs();

        }

        return dir;
    }


    private long directorySize(File file) {

        if (file == null || !file.exists()) return 0L;

        if (file.isFile()) return file.length();

        long size = 0L;

        File[] children = file.listFiles();

        if (children != null) {

            for (File child : children) {

                size += directorySize(child);

            }

        }

        return size;

    }


    public void calculateBreakdown(final BreakdownCallback callback) {

        ioExecutor.execute(() -> {

            long pdfBytes = directorySize(generatedPdfsDir());

            long cacheBytes = directorySize(appContext.getCacheDir());

            // "Other app data" = everything else app-private: filesDir minus generated_pdfs,
            // plus the no-backup files dir and the app-private external files dir if present.

            long filesDirTotal = directorySize(appContext.getFilesDir());

            long filesDirOther = Math.max(filesDirTotal - pdfBytes, 0L);

            long noBackupDir = directorySize(appContext.getNoBackupFilesDir());

            long externalFilesDir = directorySize(appContext.getExternalFilesDir(null));

            long otherBytes = filesDirOther + noBackupDir + externalFilesDir;

            final StorageBreakdown breakdown = new StorageBreakdown(pdfBytes, cacheBytes, otherBytes);

            mainHandler.post(() -> callback.onResult(breakdown));

        });
    }


    public void clearSelectedStorage(
            boolean clearGeneratedPdfs,
            boolean clearCache,
            boolean clearOther,
            final ClearCallback callback
    ) {

        ioExecutor.execute(() -> {

            boolean success = true;

            if (clearGeneratedPdfs) {

                success = deleteContentsOf(generatedPdfsDir()) && success;
            }

            if (clearCache) {

                success = deleteContentsOf(appContext.getCacheDir()) && success;
            }

            if (clearOther) {

                // Clear everything in filesDir except generated_pdfs
                File filesDir = appContext.getFilesDir();

                File[] children = filesDir.listFiles();

                if (children != null) {

                    for (File child : children) {

                        // Keep generated_pdfs if it wasn't selected
                        if (child.equals(generatedPdfsDir())) {
                            continue;
                        }

                        success = deleteRecursively(child) && success;
                    }
                }

                // Clear no-backup files
                success = deleteContentsOf(
                        appContext.getNoBackupFilesDir()
                ) && success;

                // Clear app-private external files
                File externalFiles =
                        appContext.getExternalFilesDir(null);

                success = deleteContentsOf(externalFiles) && success;
            }

            final boolean result = success;

            if (callback != null) {
                mainHandler.post(() -> callback.onResult(result));
            }
        });
    }


    public void runAutomaticCleanup(long maxAgeMillis, final CleanupCallback callback) {

        ioExecutor.execute(() -> {

            long now = System.currentTimeMillis();

            int deletedCount = 0;

            File[] files = generatedPdfsDir().listFiles();

            if (files != null) {

                for (File file : files) {

                    long age = now - file.lastModified();

                    if (file.isFile() && age >= maxAgeMillis) {

                        if (file.delete()) deletedCount++;

                    }

                }

            }

            final int result = deletedCount;

            if (callback != null) {

                mainHandler.post(() -> callback.onResult(result));

            }

        });
    }

    public void runAutomaticCleanup(CleanupCallback callback) {

        runAutomaticCleanup(AUTO_CLEANUP_MAX_AGE_MILLIS, callback);

    }

    private boolean deleteContentsOf(File dir) {

        if (dir == null || !dir.exists()) return true;

        boolean success = true;

        File[] children = dir.listFiles();

        if (children != null) {

            for (File child : children) {

                success = deleteRecursively(child) && success;

            }

        }

        return success;

    }

    private boolean deleteRecursively(File file) {

        if (file.isDirectory()) {

            File[] children = file.listFiles();

            if (children != null) {

                for (File child : children) {

                    deleteRecursively(child);

                }
            }
        }
        return file.delete();
    }

    public String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log(bytes) / Math.log(1024));
        digitGroups = Math.max(0, Math.min(digitGroups, units.length - 1));
        double value = bytes / Math.pow(1024, digitGroups);
        String pattern = digitGroups == 0 ? "%.0f %s" : "%.2f %s";
        return String.format(Locale.US, pattern, value, units[digitGroups]);
    }

    public String formatPercentage(float percentage) {
        return String.format(Locale.US, "%.1f%%", percentage);
    }

    public void shutdown() {
        ioExecutor.shutdown();
    }
}