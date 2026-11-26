package com.harsh.pdfstudio.custom_view;

import android.content.Context;
import android.graphics.Bitmap;
import  android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class EraserView extends View {

    private ZoomController zoomController;
    private ScaleGestureDetector scaleDetector;

    private final Path eraserPath = new Path();

    private int activePointerId = MotionEvent.INVALID_POINTER_ID;

    private Bitmap workingBitmap;
    private Canvas eraseCanvas;
    private Paint erasePaint;

    private Bitmap pageBitmap;

    private float eraserSize = 30f;

    private float lastX;
    private float lastY;

    private float eraserX;
    private float eraserY;
    private boolean showEraserCursor = false;

    Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    public EraserView(Context context ) {
        super(context);
        init();
    }

    public EraserView(Context context, AttributeSet attrs ) {
        super(context, attrs);
        init();
    }

    public EraserView(Context context, AttributeSet attrs, int defStyleAttr ) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        zoomController = new ZoomController();

        initGesture();

        setFocusable(true);
        setFocusableInTouchMode(true);

    }


    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.save();

        canvas.concat(
                zoomController.getMatrix()
        );

        if (workingBitmap != null) {

            canvas.drawBitmap(
                    workingBitmap,
                    0,
                    0,
                    null
            );
        }

        if (showEraserCursor) {

            drawEraserCursor(canvas, eraserX, eraserY, eraserSize);

        }

        canvas.restore();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Always give events to zoom detector.
        scaleDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                activePointerId = event.getPointerId(0);
                lastX = event.getX(0);
                lastY = event.getY(0);

                PointF start = zoomController.screenToPage(lastX, lastY);
                eraserX = start.x;
                eraserY = start.y;
                showEraserCursor = true;

                eraserPath.reset();
                eraserPath.moveTo(start.x, start.y);

                // erase a dot immediately so a tap actually erases something
                eraseAt(start.x, start.y);

                invalidate();
                return true;
            }


            case MotionEvent.ACTION_POINTER_DOWN:

                // Second finger means the user is starting
                // a pinch gesture.

                showEraserCursor = false;

                // Cancel the current eraser stroke.
                eraserPath.reset();

                return true;


            case MotionEvent.ACTION_MOVE: {

                // Never erase while zooming.
                if (event.getPointerCount() > 1
                        || scaleDetector.isInProgress()) {

                    return true;
                }

                int pointerIndex =
                        event.findPointerIndex(activePointerId);

                if (pointerIndex < 0) {
                    return true;
                }

                float currentX =
                        event.getX(pointerIndex);

                float currentY =
                        event.getY(pointerIndex);


                PointF start =
                        zoomController.screenToPage(
                                lastX,
                                lastY
                        );

                PointF end =
                        zoomController.screenToPage(
                                currentX,
                                currentY
                        );


                // Add the movement to the stroke.
                eraserPath.lineTo(
                        end.x,
                        end.y
                );

                showEraserCursor = true;

                // Actually erase this segment.
                eraseLine(
                        start.x,
                        start.y,
                        end.x,
                        end.y
                );

                eraserX = end.x;
                eraserY = end.y;


                lastX = currentX;
                lastY = currentY;

                invalidate();

                return true;
            }


            case MotionEvent.ACTION_POINTER_UP: {

                int releasedPointer =
                        event.getPointerId(event.getActionIndex());

                if (releasedPointer == activePointerId) {

                    // Active finger was released — hand off to another one.
                    activePointerId = MotionEvent.INVALID_POINTER_ID;

                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) != releasedPointer) {
                            activePointerId = event.getPointerId(i);
                            lastX = event.getX(i);
                            lastY = event.getY(i);
                            break;
                        }
                    }

                } else {


                    int activeIndex = event.findPointerIndex(activePointerId);

                    if (activeIndex >= 0) {
                        lastX = event.getX(activeIndex);
                        lastY = event.getY(activeIndex);
                    }
                }

                // Don't continue the old eraser stroke after a pinch.
                eraserPath.reset();

                PointF resync = zoomController.screenToPage(lastX, lastY);
                eraserPath.moveTo(resync.x, resync.y);

                return true;
            }


            case MotionEvent.ACTION_UP:

                // Finish the stroke.
                eraserPath.reset();

                showEraserCursor = false;

                activePointerId = MotionEvent.INVALID_POINTER_ID;

                invalidate();

                return true;


            case MotionEvent.ACTION_CANCEL:

                eraserPath.reset();

                showEraserCursor = false;

                activePointerId = MotionEvent.INVALID_POINTER_ID;

                invalidate();

                return true;
        }

        return true;
    }

    private void drawEraserCursor(Canvas canvas, float cx, float cy, float size) {

        bodyPaint.setStyle(Paint.Style.FILL);
        bodyPaint.setColor(Color.parseColor("#FFB6C1")); // pink body


        tipPaint.setStyle(Paint.Style.FILL);
        tipPaint.setColor(Color.WHITE);


        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(Math.max(2f, size * 0.04f));
        outlinePaint.setColor(Color.DKGRAY);

        canvas.save();
        canvas.translate(cx, cy);

        float halfW = size / 2f;
        float halfH = size / 3.5f; // eraser is wider than tall
        float corner = halfH * 0.4f;

        RectF bodyRect = new RectF(-halfW, -halfH, halfW, halfH);

        // main body
        canvas.drawRoundRect(bodyRect, corner, corner, bodyPaint);

        // white tip on right side
        RectF tipRect = new RectF(halfW * 0.35f, -halfH, halfW, halfH);
        canvas.drawRoundRect(tipRect, corner, corner, tipPaint);
        // square off the inner edge so it blends with body
        canvas.drawRect(halfW * 0.35f, -halfH, halfW * 0.35f + corner, halfH, tipPaint);

        // outline
        canvas.drawRoundRect(bodyRect, corner, corner, outlinePaint);

        // divider line between body and tip
        canvas.drawLine(halfW * 0.35f, -halfH, halfW * 0.35f, halfH, outlinePaint);

        canvas.restore();
    }

    private void initGesture() {

        scaleDetector =
                new ScaleGestureDetector(
                        getContext(),

                        new ScaleGestureDetector
                                .SimpleOnScaleGestureListener() {

                            @Override
                            public boolean onScale(
                                    ScaleGestureDetector detector) {

                                zoomController.zoomAt(
                                        detector.getScaleFactor(),
                                        detector.getFocusX(),
                                        detector.getFocusY()
                                );

                                invalidate();

                                return true;
                            }
                        }
                );
    }

    private void eraseAt(float pageX, float pageY) {

        if (eraseCanvas == null) {
            return;
        }

        // Eraser size is in page coordinates
        float radius = eraserSize / 2f;

        eraseCanvas.drawCircle(
                pageX,
                pageY,
                radius,
                erasePaint
        );

        invalidate();
    }

    public void setPageBitmap(Bitmap bitmap) {

        pageBitmap = bitmap;

        workingBitmap = bitmap.copy(
                Bitmap.Config.ARGB_8888,
                true
        );

        eraseCanvas = new Canvas(workingBitmap);

        erasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        erasePaint.setColor(Color.WHITE);
        erasePaint.setStyle(Paint.Style.FILL);

        invalidate();
    }

    private void eraseLine( float x1, float y1, float x2, float y2 ) {

        float distance = (float) Math.hypot( x2 - x1, y2 - y1 );

        float step = Math.max( 1f, eraserSize * 0.25f );

        int count = Math.max( 1, (int) Math.ceil(distance / step) );

        for (int i = 0; i <= count; i++) {

            float t = i / (float) count;

            float x = x1 + ( x2 - x1 ) * t;

            float y = y1 + ( y2 - y1 ) * t;

            eraseAt(x, y);
        }
    }

    public void setEraserSize(float size) {

        eraserSize = size;

        invalidate();

    }

    public void resetEraser() {

        if (pageBitmap == null) {
            return;
        }

        // 1. Stop current eraser stroke
        eraserPath.reset();

        // 2. Reset touch state
        activePointerId = MotionEvent.INVALID_POINTER_ID;

        lastX = 0f;
        lastY = 0f;

        // 3. Create a completely fresh bitmap
        Bitmap newBitmap = pageBitmap.copy(
                Bitmap.Config.ARGB_8888,
                true
        );

        if (newBitmap == null) {
            return;
        }

        // 4. Replace old working bitmap
        Bitmap oldBitmap = workingBitmap;

        workingBitmap = newBitmap;

        // 5. Create a NEW canvas attached to the new bitmap
        eraseCanvas = new Canvas(workingBitmap);

        // 6. Recreate erase paint if necessary
        if (erasePaint == null) {

            erasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            erasePaint.setStyle(Paint.Style.FILL);
        }

        // 7. Old bitmap is no longer needed
        if (oldBitmap != null
                && oldBitmap != pageBitmap
                && !oldBitmap.isRecycled()) {

            oldBitmap.recycle();
        }

        // 8. Redraw
        invalidate();
    }

    public void  setEraserColor(int color){

        erasePaint.setColor(color);

    }

}