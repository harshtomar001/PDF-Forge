package com.harsh.pdfstudio.renderer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;


import androidx.core.content.FileProvider;

import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.MergePdfErrorItem;
import com.harsh.pdfstudio.model.MergePdfItem;
import com.harsh.pdfstudio.model.MergePdfOptions;
import com.harsh.pdfstudio.model.MergeResult;
import com.harsh.pdfstudio.model.PageSizeBounds;
import com.harsh.pdfstudio.ui.MergePdfActivity;
import com.harsh.pdfstudio.utils.GenerateName;
import com.tom_roush.harmony.awt.geom.AffineTransform;
import com.tom_roush.pdfbox.io.MemoryUsageSetting;
import com.tom_roush.pdfbox.multipdf.LayerUtility;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException;
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject;
import com.tom_roush.pdfbox.util.Matrix;
import com.vertlix.utils.pdf.Page_Count;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class MergePdfRenderer {

    public enum MergeErrorType {
        INPUT_FILE_READ_ERROR,
        OUTPUT_DIRECTORY_ERROR,
        OUTPUT_CREATION_ERROR,
        CORRUPTED_PDF,
        UNKNOWN,
        PASSWORD_PROTECTED
    }

    private static final String TAG = "MergePdfRenderer";

    public MergeResult render(
            Context context,
            List<MergePdfItem> items,
            MergePdfOptions options
    ) throws Exception {

        if (items == null || items.isEmpty()) {
            return MergeResult.error(
                    new MergePdfErrorItem(
                            null,
                            MergeErrorType.UNKNOWN,
                            "No PDF files selected."
                    )
            );
        }

        if (options == null) {
            options = new MergePdfOptions(
                    MergePdfActivity.PageSizeMode.ORIGINAL,
                    MergePdfActivity.PageMargin.NONE
            );
        }

        File generatedPdfsDirectory = new File(context.getFilesDir(), "generated_pdfs");

        File mergeDirectory = new File(generatedPdfsDirectory, "merge_pdf");

        File inputDirectory = new File(mergeDirectory, "input");

        File outputDirectory = new File(mergeDirectory, "output");

        // ---------------------------------------------------------
        // Create directories
        // ---------------------------------------------------------

        if (!inputDirectory.exists() && !inputDirectory.mkdirs()) {
            return MergeResult.error(
                    new MergePdfErrorItem(
                            null,
                            MergeErrorType.OUTPUT_DIRECTORY_ERROR,
                            "Unable to create temporary directory."
                    )
            );
        }

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            return MergeResult.error(
                    new MergePdfErrorItem(
                            null,
                            MergeErrorType.OUTPUT_DIRECTORY_ERROR,
                            "Unable to create output directory."
                    )
            );
        }

        File outputFile = new File(outputDirectory, GenerateName.generateUniqueName("mrg_"));

        List<File> tempFiles = new ArrayList<>();

        try {

            for (MergePdfItem item : items) {

                File tempFile = copyUriToTempFile(context, item.getUri(), inputDirectory);

                if (tempFile == null) {
                    return MergeResult.error(
                            new MergePdfErrorItem(
                                    item,
                                    MergeErrorType.INPUT_FILE_READ_ERROR,
                                    "Unable to read this PDF."
                            )
                    );
                }

                tempFiles.add(tempFile);
            }



            PageSizeBounds pageSizeBounds = null;

            if (requiresPageSizeCalculation(options.getPageSizeMode())) {

                pageSizeBounds = calculatePageSizeBounds(tempFiles);

            }



            try (
                    PDDocument outputDocument = new PDDocument(MemoryUsageSetting.setupTempFileOnly())
            ) {

                for (int i = 0; i < tempFiles.size(); i++) {

                    File tempFile = tempFiles.get(i);

                    try {

                        mergeDocument(outputDocument, tempFile, options, pageSizeBounds);

                    }
                    catch (InvalidPasswordException e) {

                        return MergeResult.error(
                                new MergePdfErrorItem(
                                        items.get(i),
                                        MergeErrorType.PASSWORD_PROTECTED,
                                        "This PDF is password protected."
                                )
                        );

                    }

                    catch (IOException e) {

                        return MergeResult.error(
                                new MergePdfErrorItem(
                                        items.get(i),
                                        MergeErrorType.CORRUPTED_PDF,
                                        "This PDF could not be processed."
                                )
                        );

                    }
                }

                outputDocument.save(outputFile);

            }


            if (!outputFile.exists() || outputFile.length() == 0) {

                return MergeResult.error(
                        new MergePdfErrorItem(
                                null,
                                MergeErrorType.OUTPUT_CREATION_ERROR,
                                "The merged PDF could not be created."
                        )
                );

            }


            Uri outputUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    outputFile
            );



            FileItem result = new FileItem(
                    outputFile.getName(),
                    outputUri,
                    outputFile.length(),
                    Page_Count.getPdfPageCount(context, outputUri)
            );

            return MergeResult.success(result);

        }
        finally {

            deleteTempFiles(tempFiles);

        }
    }

    /**
     * Determines whether the selected page-size mode requires
     * document-wide dimensions from all input PDFs. Only FIT_*
     * modes need this; ORIGINAL and the fixed standard sizes do not.
     */
    private boolean requiresPageSizeCalculation(MergePdfActivity.PageSizeMode mode) {

        if (mode == null) {
            return false;
        }

        switch (mode) {
            case FIT_SMALLEST_WIDTH:
            case FIT_SMALLEST_HEIGHT:
            case FIT_LARGEST_WIDTH:
            case FIT_LARGEST_HEIGHT:
            case FIT_SMALLEST_BOTH:
            case FIT_LARGEST_BOTH:
                return true;
            default:
                return false;
        }
    }

    private void mergeDocument(
            PDDocument outputDocument,
            File inputFile,
            MergePdfOptions options,
            PageSizeBounds pageSizeBounds
    ) throws IOException {

        try (
                PDDocument inputDocument =
                        PDDocument.load(inputFile, MemoryUsageSetting.setupTempFileOnly())
        ) {

            for (PDPage sourcePage : inputDocument.getPages()) {

                mergePage(
                        outputDocument,
                        sourcePage,
                        inputDocument,
                        options,
                        pageSizeBounds
                );
            }
        }
    }

    private File copyUriToTempFile(
            Context context,
            Uri uri,
            File directory
    ) throws IOException {

        if (uri == null) {
            return null;
        }

        String fileName = "input_" + System.nanoTime() + ".pdf";

        File tempFile = new File(directory, fileName);

        try (
                InputStream input = context.getContentResolver().openInputStream(uri);
                OutputStream output = new FileOutputStream(tempFile)
        ) {

            if (input == null) {
                return null;
            }

            byte[] buffer = new byte[64 * 1024];
            int read;

            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }

        return tempFile;
    }

    /**
     * Result of resolving a page's target size.
     *
     * `size` is the page's content-only size for FIT_* modes (i.e.
     * the source page already scaled by the resolved factor, aspect
     * preserved) — or the final, already-correct page size for
     * ORIGINAL / standard modes.
     *
     * `fitMode` tells mergePage() whether margin must be applied
     * additively on top of `size` (FIT_*) or left for drawPage() to
     * inset within a fixed `size` (ORIGINAL / standard).
     */
    private static final class PageLayout {
        final PDRectangle size;
        final boolean fitMode;

        PageLayout(PDRectangle size, boolean fitMode) {
            this.size = size;
            this.fitMode = fitMode;
        }
    }

    private void mergePage(
            PDDocument outputDocument,
            PDPage sourcePage,
            PDDocument sourceDocument,
            MergePdfOptions options,
            PageSizeBounds pageSizeBounds
    ) throws IOException {

        // =========================================================
        // Source page dimensions (raw MediaBox — NOT rotation-swapped)
        // =========================================================

        PDRectangle sourceBox = sourcePage.getMediaBox();

        float originalWidth = sourceBox.getWidth();
        float originalHeight = sourceBox.getHeight();

        int rotation = normalizeRotation(sourcePage.getRotation());

        boolean rotated = rotation == 90 || rotation == 270;

        // Visual dimensions — what the page looks like once rotation
        // is taken into account. Used for scaling and page sizing.
        float visualWidth = rotated ? originalHeight : originalWidth;
        float visualHeight = rotated ? originalWidth : originalHeight;

        PageLayout layout = getTargetPageSize(
                options.getPageSizeMode(),
                visualWidth,
                visualHeight,
                pageSizeBounds
        );

        float margin = getMargin(options.getMargin());

        PDRectangle finalPageSize;

        if (layout.fitMode) {
            // The page IS the scaled source content — margin (if any)
            // is added explicitly around it. With PageMargin.NONE this
            // is a no-op: finalPageSize == layout.size exactly.
            float pageWidth = layout.size.getWidth() + (2f * margin);
            float pageHeight = layout.size.getHeight() + (2f * margin);
            finalPageSize = new PDRectangle(pageWidth, pageHeight);
        } else {
            // ORIGINAL / standard sizes: page size is fixed and
            // independent of the source page; margin is an inset
            // handled inside drawPage() when fitting the content.
            finalPageSize = layout.size;
        }

        PDPage outputPage = new PDPage(finalPageSize);
        outputPage.setRotation(0);

        outputDocument.addPage(outputPage);

        drawPage(
                outputDocument,
                outputPage,
                sourcePage,
                sourceDocument,
                originalWidth,
                originalHeight,
                visualWidth,
                visualHeight,
                rotation,
                margin
        );
    }

    private int normalizeRotation(int rotation) {

        rotation %= 360;

        if (rotation < 0) {
            rotation += 360;
        }

        return rotation;
    }

    /**
     * Resolves the target page layout for a single source page.
     *
     * For ORIGINAL / A4 / A3 / LETTER / LEGAL: page size is fixed
     * (visual size, or orientation-aware standard size) and does not
     * depend on `bounds` — no unnecessary bounds usage.
     *
     * For FIT_* modes: the scale factor is resolved first, from the
     * document-wide bounds, then applied identically to both width
     * and height. The returned size is the scaled content itself —
     * never an independently-sized canvas.
     */
    private PageLayout getTargetPageSize(
            MergePdfActivity.PageSizeMode mode,
            float visualWidth,
            float visualHeight,
            PageSizeBounds bounds
    ) {

        if (mode == null) {
            return new PageLayout(new PDRectangle(visualWidth, visualHeight), false);
        }

        switch (mode) {

            // =====================================================
            // ORIGINAL — page matches the page's visual size exactly
            // =====================================================

            case ORIGINAL:
                return new PageLayout(new PDRectangle(visualWidth, visualHeight), false);

            // =====================================================
            // STANDARD PAGE SIZES — fixed, orientation-aware
            // =====================================================

            case A4:
                return new PageLayout(
                        createOrientationAwareSize(PDRectangle.A4, visualWidth, visualHeight), false);

            case A3:
                return new PageLayout(
                        createOrientationAwareSize(PDRectangle.A3, visualWidth, visualHeight), false);

            case LETTER:
                return new PageLayout(
                        createOrientationAwareSize(PDRectangle.LETTER, visualWidth, visualHeight), false);

            case LEGAL:
                return new PageLayout(
                        createOrientationAwareSize(PDRectangle.LEGAL, visualWidth, visualHeight), false);

            // =====================================================
            // FIT modes — resolve scale from document-wide bounds
            // FIRST, then apply that SAME scale to both dimensions.
            // The result is the scaled content itself.
            // =====================================================

            case FIT_SMALLEST_WIDTH: {
                requireBounds(bounds);
                float scale = bounds.getSmallestWidth() / visualWidth;
                return fitLayout(visualWidth, visualHeight, scale);
            }

            case FIT_LARGEST_WIDTH: {
                requireBounds(bounds);
                float scale = bounds.getLargestWidth() / visualWidth;
                return fitLayout(visualWidth, visualHeight, scale);
            }

            case FIT_SMALLEST_HEIGHT: {
                requireBounds(bounds);
                float scale = bounds.getSmallestHeight() / visualHeight;
                return fitLayout(visualWidth, visualHeight, scale);
            }

            case FIT_LARGEST_HEIGHT: {
                requireBounds(bounds);
                float scale = bounds.getLargestHeight() / visualHeight;
                return fitLayout(visualWidth, visualHeight, scale);
            }

            case FIT_SMALLEST_BOTH: {
                requireBounds(bounds);
                float widthScale = bounds.getSmallestWidth() / visualWidth;
                float heightScale = bounds.getSmallestHeight() / visualHeight;
                // min() so the ENTIRE page fits within both smallest
                // bounds — never crops.
                float scale = Math.min(widthScale, heightScale);
                return fitLayout(visualWidth, visualHeight, scale);
            }

            case FIT_LARGEST_BOTH: {
                requireBounds(bounds);
                float widthScale = bounds.getLargestWidth() / visualWidth;
                float heightScale = bounds.getLargestHeight() / visualHeight;
                // min() (NOT max()) — allows enlargement while still
                // guaranteeing the complete source page fits without
                // being cropped.
                float scale = Math.min(widthScale, heightScale);
                return fitLayout(visualWidth, visualHeight, scale);
            }

            default:
                return new PageLayout(new PDRectangle(visualWidth, visualHeight), false);
        }
    }

    /**
     * Builds the FIT-mode content layout: newWidth/newHeight are the
     * SAME scale factor applied to both axes, so aspect ratio is
     * always preserved. Scale is intentionally NOT clamped to <= 1 —
     * enlargement is required for LARGEST_* modes.
     */
    private PageLayout fitLayout(float visualWidth, float visualHeight, float scale) {

        float newWidth = visualWidth * scale;
        float newHeight = visualHeight * scale;

        return new PageLayout(new PDRectangle(newWidth, newHeight), true);
    }

    private void requireBounds(PageSizeBounds bounds) {

        if (bounds == null) {
            throw new IllegalStateException(
                    "Page size bounds are required for FIT mode."
            );
        }
    }

    private PDRectangle createOrientationAwareSize(
            PDRectangle standard,
            float visualWidth,
            float visualHeight
    ) {

        boolean sourceLandscape = visualWidth > visualHeight;
        boolean standardLandscape = standard.getWidth() > standard.getHeight();

        if (sourceLandscape != standardLandscape) {
            return new PDRectangle(standard.getHeight(), standard.getWidth());
        }

        return new PDRectangle(standard.getWidth(), standard.getHeight());
    }

    private float getMargin(MergePdfActivity.PageMargin margin) {

        if (margin == null) {
            return 0f;
        }

        switch (margin) {
            case NONE:
                return 0f;
            case SMALL:
                return 18f;
            case MEDIUM:
                return 36f;
            case LARGE:
                return 54f;
            default:
                return 0f;
        }
    }

    /**
     * Draws the source page's content into the output page.
     *
     * originalWidth/originalHeight are the page's raw MediaBox
     * dimensions — the coordinate space the imported form actually
     * lives in. visualWidth/visualHeight are the rotation-aware
     * dimensions used for scaling against the output page.
     *
     * For FIT modes, outputPage was built as
     * (scaledContent + 2*margin), so scaleX/scaleY below resolve to
     * the SAME value the scale was originally computed with — the
     * content exactly fills the page (minus margin), no incidental
     * whitespace. For ORIGINAL/standard modes, the page size is
     * independent of the source, so this fits-and-centers the
     * content within the margin-reduced area, same as before.
     */
    private void drawPage(
            PDDocument outputDocument,
            PDPage outputPage,
            PDPage sourcePage,
            PDDocument sourceDocument,
            float originalWidth,
            float originalHeight,
            float visualWidth,
            float visualHeight,
            int rotation,
            float margin
    ) throws IOException {

        float targetWidth = outputPage.getMediaBox().getWidth();
        float targetHeight = outputPage.getMediaBox().getHeight();

        // With margin == 0 (PageMargin.NONE) these equal the full
        // target dimensions exactly — no hidden inset is introduced.
        float availableWidth = Math.max(1f, targetWidth - (2f * margin));
        float availableHeight = Math.max(1f, targetHeight - (2f * margin));

        float scaleX = availableWidth / visualWidth;
        float scaleY = availableHeight / visualHeight;

        // min() (not max()) guarantees the entire source page stays
        // visible; also allows scale > 1 so smaller pages enlarge to
        // fill a larger target. For FIT modes scaleX == scaleY by
        // construction, so this just recovers that same scale.
        float scale = Math.min(scaleX, scaleY);

        float renderedWidth = visualWidth * scale;
        float renderedHeight = visualHeight * scale;

        float x = (targetWidth - renderedWidth) / 2f;
        float y = (targetHeight - renderedHeight) / 2f;

        // ---------------------------------------------------------
        // Build the transform:
        //  1. Rotate/translate the ORIGINAL content into "visual"
        //     orientation (a visualWidth x visualHeight box at the
        //     origin).
        //  2. Scale that box by `scale`.
        //  3. Translate it into position on the target page.
        // AffineTransform composes right-to-left, so the rotation
        // step is written last, applying to the content first.
        // ---------------------------------------------------------

        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.scale(scale, scale);

        switch (rotation) {
            case 90:
                at.translate(0, originalWidth);
                at.rotate(-Math.PI / 2);
                break;
            case 180:
                at.translate(originalWidth, originalHeight);
                at.rotate(Math.PI);
                break;
            case 270:
                at.translate(originalHeight, 0);
                at.rotate(Math.PI / 2);
                break;
            default:
                // 0 degrees — no rotation compensation needed.
                break;
        }

        Matrix matrix = new Matrix(at);

        LayerUtility layerUtility = new LayerUtility(outputDocument);

        PDFormXObject form = layerUtility.importPageAsForm(sourceDocument, sourcePage);

        try (
                PDPageContentStream contentStream = new PDPageContentStream(
                        outputDocument,
                        outputPage,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                )
        ) {

            contentStream.transform(matrix);
            contentStream.drawForm(form);
        }

        // NOTE: if you add a page border, draw it here using a
        // separate PDPageContentStream call against the full
        // targetWidth/targetHeight rectangle, AFTER this content is
        // drawn. Never fold border width into `margin` above — that
        // would silently re-introduce a forced inset even when
        // PageMargin.NONE is selected.
    }

    /**
     * Calculates document-wide visual page dimensions.
     *
     * IMPORTANT: only called once per merge, and only when
     * requiresPageSizeCalculation() returns true for the selected mode.
     */
    private PageSizeBounds calculatePageSizeBounds(List<File> pdfFiles) throws IOException {

        PageSizeBounds bounds = new PageSizeBounds();

        for (File file : pdfFiles) {

            try (
                    PDDocument document =
                            PDDocument.load(file, MemoryUsageSetting.setupTempFileOnly())
            ) {

                for (PDPage page : document.getPages()) {

                    PDRectangle mediaBox = page.getMediaBox();

                    float width = mediaBox.getWidth();
                    float height = mediaBox.getHeight();

                    int rotation = normalizeRotation(page.getRotation());
                    boolean rotated = rotation == 90 || rotation == 270;

                    float visualWidth = rotated ? height : width;
                    float visualHeight = rotated ? width : height;

                    bounds.include(visualWidth, visualHeight);
                }
            }
        }

        return bounds;
    }

    private void deleteTempFiles(List<File> files) {

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file != null && file.exists()) {

                try {
                    file.delete();
                } catch (Exception ignored) {
                    // Best-effort cleanup only.
                }
            }
        }
    }
}