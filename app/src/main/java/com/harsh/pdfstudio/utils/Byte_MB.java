package com.harsh.pdfstudio.utils;

public class Byte_MB {

    public static String convertByte(long fileSize) {
        double kb = 1024.0;
        double mb = 1024.0 * 1024.0;

        double MB = (double) fileSize / mb;
        double KB = (double) fileSize / kb;

        if (MB >= 1) {

            MB = Math.round(MB * 100.0) / 100.0;
            return MB + " MB";
        } else if (KB >= 1) {

            KB = Math.round(KB * 10.0) / 10.0;
            return KB + " KB";
        } else {
            // Bytes, no rounding needed
            return fileSize + " Byte";
        }
    }

    public static int totalImages(long size){

        double photoSize = 150 * 1024.0; // 150 KB

        return (int)(size / photoSize);
    }
    public static int totalVideoMinutes(long savedBytes) {

        double savedMB = savedBytes / (1024.0 * 1024.0);

        return (int) (savedMB / 10.0);
    }
}