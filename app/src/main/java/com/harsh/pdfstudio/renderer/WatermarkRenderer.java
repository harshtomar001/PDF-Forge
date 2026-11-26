package com.harsh.pdfstudio.renderer;

import static com.harsh.pdfstudio.fragments.Edit_Pdf_Edit_Page.WatermarkPosition;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import com.harsh.pdfstudio.model.Coordinates;
import com.harsh.pdfstudio.operations.WatermarkOperation;
import com.harsh.pdfstudio.utils.Dp_to_Px;
import com.harsh.pdfstudio.utils.GenerateBitmapFromImgUri;
import java.io.IOException;


public class WatermarkRenderer {

    public static void applyWatermark(
            Bitmap pageBitmap,
            Canvas canvas,
            WatermarkOperation operation) throws IOException {

        Bitmap watermarkBitmap = null;


        try {

            float pageWidth = pageBitmap.getWidth();
            float pageHeight = pageBitmap.getHeight();

            if (operation.isImageWatermark()) {

                watermarkBitmap = getScaledImageBitmap(
                        operation.getContext(),
                        operation.getWatermarkImageUri(),
                        pageWidth,
                        operation.getSize()
                );

                if (watermarkBitmap == null) {
                    throw new IOException("Failed to decode watermark image.");
                }
            }

            Coordinates coordinates = getPositionCoordinates(
                    operation.getContext() ,
                    pageWidth,
                    pageHeight,
                    operation.getCurrentPosition(),
                    operation.isImageWatermark(),
                    watermarkBitmap,
                    operation.getWatermarkText(),
                    operation.getSize()
            );

            drawWatermark(
                    operation.getContext(),

                    canvas,
                    operation.getWatermarkText(),
                    operation.isImageWatermark(),
                    watermarkBitmap,
                    operation.getSize(),
                    operation.getRotation(),
                    operation.getOpacity(),
                    coordinates
            );

        } finally {

            if (watermarkBitmap != null && !watermarkBitmap.isRecycled()) {
                // watermarkBitmap.recycle();
            }

        }
    }


    private static Coordinates getPositionCoordinates(Context context, float width,float height,
                                                      WatermarkPosition position,
                                                      boolean isImageWatermark,
                                                      Bitmap watermarkBitmap,
                                                      String watermarkText, float size) {

        float watermarkHeight = 0f;
        float watermarkWidth = 0f;

        if (isImageWatermark) {

            watermarkHeight = watermarkBitmap.getHeight();
            watermarkWidth = watermarkBitmap.getWidth();

        } else {

            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            textPaint.setTextSize(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            size,
                            context.getResources().getDisplayMetrics()
                    )
            );

            watermarkWidth = (int) Math.ceil(textPaint.measureText(watermarkText));

            Rect textBounds = new Rect();
            textPaint.getTextBounds(watermarkText, 0, watermarkText.length(), textBounds);

            // textBounds.top is negative (distance above baseline); use its magnitude as ascent
            watermarkHeight = textBounds.height();
        }

        Coordinates coordinates;

        int margin = Dp_to_Px.dpToPx(8);

        switch (position) {
            case TOP_LEFT:
                coordinates = new Coordinates(margin, margin);
                break;

            case TOP_CENTER:
                float x = (width / 2f) - (watermarkWidth / 2f);
                coordinates = new Coordinates(x, margin);
                break;

            case TOP_RIGHT:
                float x1 = width - watermarkWidth - margin;
                coordinates = new Coordinates(x1, margin);
                break;

            case CENTER_LEFT:
                float y = (height / 2f) - (watermarkHeight / 2f);
                coordinates = new Coordinates(margin, y);
                break;

            case CENTER:
                float x2 = (width / 2f) - (watermarkWidth / 2f);
                float y2 = (height / 2f) - (watermarkHeight / 2f);
                coordinates = new Coordinates(x2, y2);
                break;

            case CENTER_RIGHT:
                float x3 = width - watermarkWidth - margin;
                float y3 = (height / 2f) - (watermarkHeight / 2f);
                coordinates = new Coordinates(x3, y3);
                break;

            case BOTTOM_LEFT:
                float y4 = height - watermarkHeight - margin;
                coordinates = new Coordinates(margin, y4);
                break;

            case BOTTOM_CENTER:
                float x4 = (width / 2f) - (watermarkWidth / 2f);
                float y5 = height - watermarkHeight - margin;
                coordinates = new Coordinates(x4, y5);
                break;

            case BOTTOM_RIGHT:
                float x5 = width - watermarkWidth - margin;
                float y6 = height - watermarkHeight - margin;
                coordinates = new Coordinates(x5, y6);
                break;

            default:
                coordinates = new Coordinates(0, 0);
                break;
        }

        coordinates.setWatermarkHeight(watermarkHeight);
        coordinates.setWatermarkWidth(watermarkWidth);

        Log.d("Watermark",
                "x=" + coordinates.getWatermarkHeight() +
                        " y=" + coordinates.getWatermarkWidth() +
                        " page=" + coordinates.getX_coor() +
                        "x" + coordinates.getY_coor());


        return coordinates;
    }


    private static void drawWatermark(Context context, Canvas canvas,
                                         String watermarkText,
                                         boolean isImageWatermark,
                                         Bitmap iconBitmap,
                                         float size, int rotation,
                                         float opacity,
                                      Coordinates coordinates) {

        // x, y here represent the TOP-LEFT of the watermark's bounding box

        float x = coordinates.getX_coor();

        float y = coordinates.getY_coor();

        float watermarkWidth = coordinates.getWatermarkWidth();

        float watermarkHeight = coordinates.getWatermarkHeight();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Center of the watermark's bounding box — used as a consistent rotation pivot

        float pivotX = x + watermarkWidth / 2f;

        float pivotY = y + watermarkHeight / 2f;

        if (isImageWatermark) {
            paint.setAlpha((int) (opacity * 255));

            canvas.save();

            canvas.rotate(rotation, pivotX, pivotY);

            if (iconBitmap != null && !iconBitmap.isRecycled()) {
                canvas.drawBitmap(iconBitmap, x, y, paint);
            }

            canvas.restore();

        } else {

            int color = Color.BLACK;

            paint.setColor(Color.argb(
                    (int)(opacity * 255),
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
            ));

            float textSizePx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    size,
                    context.getResources().getDisplayMetrics()
            );

            paint.setTextSize(textSizePx);

            // Recompute ascent to convert top-of-box y into a baseline y

            Rect textBounds = new Rect();

            paint.getTextBounds(watermarkText, 0, watermarkText.length(), textBounds);

            float baselineY = y - textBounds.top; // textBounds.top is negative

            canvas.save();

            canvas.rotate(rotation, pivotX, pivotY);


            canvas.drawText(watermarkText, x, baselineY, paint);

            canvas.restore();
        }

    }

    private static Bitmap getScaledImageBitmap(Context context,
                                               Uri iconImage,
                                               float originalBitmapWidth,
                                               float size) throws IOException {

        Bitmap imageBitmap = null;
        imageBitmap = GenerateBitmapFromImgUri.generateBitmapFromUri(context, iconImage, 1f);

        float desiredWidth = originalBitmapWidth * (size / 100f);

        float aspectRatio = (float) imageBitmap.getHeight() / imageBitmap.getWidth();

        int targetWidth = Math.round(desiredWidth);
        int targetHeight = Math.round(targetWidth * aspectRatio);

        imageBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                targetWidth,
                targetHeight,
                true
        );

        return imageBitmap;

    }



}
