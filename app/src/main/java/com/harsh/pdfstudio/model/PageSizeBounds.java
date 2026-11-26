package com.harsh.pdfstudio.model;

/**
 * Holds document-wide visual page dimensions, gathered once across
 * every input PDF, and only when a FIT_* page-size mode requires it.
 *
 * All widths/heights stored here are already rotation-adjusted
 * ("visual") dimensions — see MergePdfRenderer.calculatePageSizeBounds().
 */
public class PageSizeBounds {

    private float smallestWidth = Float.MAX_VALUE;
    private float smallestHeight = Float.MAX_VALUE;
    private float largestWidth = Float.MIN_VALUE;
    private float largestHeight = Float.MIN_VALUE;

    public void include(float width, float height) {
        if (width < smallestWidth) smallestWidth = width;
        if (height < smallestHeight) smallestHeight = height;
        if (width > largestWidth) largestWidth = width;
        if (height > largestHeight) largestHeight = height;
    }

    public float getSmallestWidth() { return smallestWidth; }
    public float getSmallestHeight() { return smallestHeight; }
    public float getLargestWidth() { return largestWidth; }
    public float getLargestHeight() { return largestHeight; }
}
