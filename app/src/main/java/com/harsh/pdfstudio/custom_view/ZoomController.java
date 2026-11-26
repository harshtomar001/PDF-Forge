package com.harsh.pdfstudio.custom_view;

import android.graphics.Matrix;
import android.graphics.PointF;

public class ZoomController {

    private final Matrix matrix = new Matrix();
    private final Matrix inverseMatrix = new Matrix();

    private float zoom = 1f;

    private float panX = 0f;
    private float panY = 0f;

    private static final float MIN_ZOOM = 1f;
    private static final float MAX_ZOOM = 5f;


    public Matrix getMatrix() {
        return matrix;
    }


    public float getZoom() {
        return zoom;
    }


    public float getPanX() {
        return panX;
    }


    public float getPanY() {
        return panY;
    }


    public void zoomAt(
            float scaleFactor,
            float focusX,
            float focusY
    ) {

        float oldZoom = zoom;

        float newZoom =
                oldZoom * scaleFactor;

        newZoom = Math.max(
                MIN_ZOOM,
                Math.min(MAX_ZOOM, newZoom)
        );

        // Actual zoom change
        float actualScale =
                newZoom / oldZoom;

        /*
         * Keep the page point underneath
         * the user's fingers stationary.
         */
        panX =
                focusX
                        - actualScale
                        * (focusX - panX);

        panY =
                focusY
                        - actualScale
                        * (focusY - panY);

        zoom = newZoom;


        /*
         * When returning to the original zoom,
         * remove accumulated zoom translation.
         */
        if (Math.abs(zoom - MIN_ZOOM) < 0.001f) {

            zoom = MIN_ZOOM;

            panX = 0f;
            panY = 0f;
        }


        updateMatrix();
    }


    public void pan(
            float dx,
            float dy
    ) {

        panX += dx;
        panY += dy;

        updateMatrix();
    }


    private void updateMatrix() {

        matrix.reset();

        matrix.postScale(
                zoom,
                zoom
        );

        matrix.postTranslate(
                panX,
                panY
        );
    }


    public PointF screenToPage(
            float screenX,
            float screenY
    ) {

        matrix.invert(inverseMatrix);

        float[] point = {
                screenX,
                screenY
        };

        inverseMatrix.mapPoints(point);

        return new PointF(
                point[0],
                point[1]
        );
    }


    public PointF pageToScreen(
            float pageX,
            float pageY
    ) {

        float[] point = {
                pageX,
                pageY
        };

        matrix.mapPoints(point);

        return new PointF(
                point[0],
                point[1]
        );
    }


    public void reset() {

        zoom = MIN_ZOOM;

        panX = 0f;
        panY = 0f;

        updateMatrix();
    }
}

