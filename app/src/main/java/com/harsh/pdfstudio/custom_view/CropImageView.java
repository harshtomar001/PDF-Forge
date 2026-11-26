package com.harsh.pdfstudio.custom_view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CropImageView extends View {

    private Bitmap bitmap;

    private final RectF cropRect = new RectF(); //  coordinates of the crop layout

    private final RectF bitmapRect = new RectF(); //  coordinates of the  pdf bitmap page

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //  paint for the bitmap

    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //  overlay paint

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float lastTouchX;
    private float lastTouchY;

    private boolean isDragging = false;
    private enum Handle {

        NONE,

        TOP_LEFT,
        TOP,
        TOP_RIGHT,

        LEFT,
        RIGHT,

        BOTTOM_LEFT,
        BOTTOM,
        BOTTOM_RIGHT

    }
    private Handle activeHandle = Handle.NONE;

    private static final float HANDLE_TOUCH_RADIUS = 50f;

    private static final float MIN_CROP_SIZE = 120f;
    private boolean maintainAspectRatio = false;
    private final RectF initialCropRect = new RectF();

    private float aspectRatio = 1f;
    private static final float HANDLE_RADIUS = 12f; // the dot  on the lines of the crop layout
    private OnCropChangedListener listener;
    public void setOnCropChangedListener(
            OnCropChangedListener listener){

        this.listener = listener;

    }



    public CropImageView(Context context) {
        super(context);
        init();

    }

    public CropImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        overlayPaint.setColor(0x88000000);

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setStyle(Paint.Style.STROKE);// to make the outline only

        gridPaint.setColor(0xAAFFFFFF);
        gridPaint.setStrokeWidth(2f);

        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);

        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
        bitmapPaint.setAntiAlias(true);

        setLayerType(LAYER_TYPE_HARDWARE, null);

    }

    public Bitmap getCroppedBitmap() {

        if (bitmap == null)
            return null;

        Rect crop = getBitmapCropRect();

        return Bitmap.createBitmap(
                bitmap,
                crop.left,
                crop.top,
                crop.width(),
                crop.height()
        );
    }

    public void setBitmap(Bitmap bitmap) {

        this.bitmap = bitmap;
        Log.d("Bitmap", "Config = " + bitmap.getConfig());
        Log.d("Bitmap", "Alpha = " + bitmap.hasAlpha());

        Log.d("Paint", "alpha = " + bitmapPaint.getAlpha());
        Log.d("Paint", "alpha = " + bitmapPaint.getColorFilter());

        calculateBitmapRect();

        invalidate();

    }

    public RectF getCropRect() {

        return new RectF(cropRect);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        calculateBitmapRect();

    }

    private void calculateBitmapRect() {

        if (bitmap == null || getWidth() == 0 || getHeight() == 0)
            return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();

        float scale = Math.min(viewWidth / bitmapWidth,
                viewHeight / bitmapHeight);

        float drawWidth = bitmapWidth * scale;
        float drawHeight = bitmapHeight * scale;

        float left = (viewWidth - drawWidth) / 2f;
        float top = (viewHeight - drawHeight) / 2f;

        bitmapRect.set(
                left,
                top,
                left + drawWidth,
                top + drawHeight
        );

        float margin = 60f;
        cropRect.set(
                bitmapRect.left + margin,
                bitmapRect.top + margin,
                bitmapRect.right - margin,
                bitmapRect.bottom - margin
        );//  a little bit inside the bitmap react

        initialCropRect.set(cropRect);

    }

    public boolean isModified() {

        return cropRect.left   != initialCropRect.left ||
                cropRect.top    != initialCropRect.top ||
                cropRect.right  != initialCropRect.right ||
                cropRect.bottom != initialCropRect.bottom;

    }


    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (bitmap == null) return;

        Paint backgroundPaint = new Paint();

        backgroundPaint.setColor(Color.WHITE);

        canvas.drawRect(bitmapRect, backgroundPaint);

        canvas.drawBitmap(bitmap,
                null,
                bitmapRect,
                bitmapPaint);

        drawOverlay(canvas);

        drawGrid(canvas);

        drawHandles(canvas);

    }

    private void drawOverlay(Canvas canvas) {

        canvas.drawRect(
                bitmapRect.left,
                bitmapRect.top,
                bitmapRect.right,
                cropRect.top,
                overlayPaint);

        canvas.drawRect(
                bitmapRect.left,
                cropRect.bottom,
                bitmapRect.right,
                bitmapRect.bottom,
                overlayPaint);

        canvas.drawRect(
                bitmapRect.left,
                cropRect.top,
                cropRect.left,
                cropRect.bottom,
                overlayPaint);

        canvas.drawRect(
                cropRect.right,
                cropRect.top,
                bitmapRect.right,
                cropRect.bottom,
                overlayPaint);

        canvas.drawRect(cropRect, borderPaint);

    }

    private void drawGrid(Canvas canvas) {

        float thirdWidth = cropRect.width() / 3f;

        float thirdHeight = cropRect.height() / 3f;

        canvas.drawLine(
                cropRect.left + thirdWidth,
                cropRect.top,
                cropRect.left + thirdWidth,
                cropRect.bottom,
                gridPaint);

        canvas.drawLine(
                cropRect.left + thirdWidth * 2,
                cropRect.top,
                cropRect.left + thirdWidth * 2,
                cropRect.bottom,
                gridPaint);

        canvas.drawLine(
                cropRect.left,
                cropRect.top + thirdHeight,
                cropRect.right,
                cropRect.top + thirdHeight,
                gridPaint);

        canvas.drawLine(
                cropRect.left,
                cropRect.top + thirdHeight * 2,
                cropRect.right,
                cropRect.top + thirdHeight * 2,
                gridPaint);

    }

    private void drawHandles(Canvas canvas) {

        drawHandle(canvas, cropRect.left, cropRect.top);

        drawHandle(canvas, cropRect.centerX(), cropRect.top);

        drawHandle(canvas, cropRect.right, cropRect.top);

        drawHandle(canvas, cropRect.left, cropRect.centerY());

        drawHandle(canvas, cropRect.right, cropRect.centerY());

        drawHandle(canvas, cropRect.left, cropRect.bottom);

        drawHandle(canvas, cropRect.centerX(), cropRect.bottom);

        drawHandle(canvas, cropRect.right, cropRect.bottom);

    }

    private void drawHandle(Canvas canvas, float x, float y) {
        Paint handleStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        handleStroke.setStyle(Paint.Style.STROKE);
        handleStroke.setStrokeWidth(3f);
        handleStroke.setColor(Color.WHITE);

        Paint handleFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        handleFill.setColor(Color.parseColor("#4285F4"));

        canvas.drawCircle(
                x,
                y,
                HANDLE_RADIUS,
                handlePaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (bitmap == null)
            return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                activeHandle = getTouchedHandle(x, y);

                if (activeHandle != Handle.NONE) {

                    lastTouchX = x;
                    lastTouchY = y;

                    return true;
                }

                if (cropRect.contains(x, y)) {

                    isDragging = true;

                    lastTouchX = x;
                    lastTouchY = y;

                    return true;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                if (activeHandle != Handle.NONE) {

                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    resizeCropRect(dx, dy);

                    lastTouchX = x;
                    lastTouchY = y;

                    invalidate();

                    if (listener != null) {

                        listener.onCropChanged(getBitmapCropRect());

                        listener.isCropChanged(isModified());
                    }

                    return true;

                }


                if (isDragging) {

                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    moveCropRect(dx, dy);

                    lastTouchX = x;
                    lastTouchY = y;

                    invalidate();

                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                activeHandle = Handle.NONE;
                isDragging = false;

                break;
        }

        return super.onTouchEvent(event);
    }
    private void moveCropRect(float dx, float dy) {

        cropRect.offset(
                Math.round(dx),
                Math.round(dy)
        );

        listener.isCropChanged(isModified());
        // Left boundary
        if (cropRect.left < bitmapRect.left) {

            cropRect.offset(bitmapRect.left - cropRect.left, 0);

        }

        // Right boundary
        if (cropRect.right > bitmapRect.right) {

            cropRect.offset(bitmapRect.right - cropRect.right, 0);

        }

        // Top boundary
        if (cropRect.top < bitmapRect.top) {

            cropRect.offset(0, bitmapRect.top - cropRect.top);

        }

        // Bottom boundary
        if (cropRect.bottom > bitmapRect.bottom) {

            cropRect.offset(0, bitmapRect.bottom - cropRect.bottom);

        }

    }
    private Handle getTouchedHandle(float x, float y) {

        if (isInsideHandle(x, y, cropRect.left, cropRect.top))
            return Handle.TOP_LEFT;

        if (isInsideHandle(x, y, cropRect.centerX(), cropRect.top))
            return Handle.TOP;

        if (isInsideHandle(x, y, cropRect.right, cropRect.top))
            return Handle.TOP_RIGHT;

        if (isInsideHandle(x, y, cropRect.left, cropRect.centerY()))
            return Handle.LEFT;

        if (isInsideHandle(x, y, cropRect.right, cropRect.centerY()))
            return Handle.RIGHT;

        if (isInsideHandle(x, y, cropRect.left, cropRect.bottom))
            return Handle.BOTTOM_LEFT;

        if (isInsideHandle(x, y, cropRect.centerX(), cropRect.bottom))
            return Handle.BOTTOM;

        if (isInsideHandle(x, y, cropRect.right, cropRect.bottom))
            return Handle.BOTTOM_RIGHT;

        return Handle.NONE;
    }

    private boolean isInsideHandle(float touchX, float touchY, float handleX,float handleY) {

        return Math.hypot(
                touchX - handleX,
                touchY - handleY
        ) <= HANDLE_TOUCH_RADIUS;

    }
    private void resizeCropRect(float dx, float dy) {

        switch (activeHandle) {

            case TOP_LEFT:

                cropRect.left += dx;
                cropRect.top += dy;
                break;

            case TOP:

                cropRect.top += dy;
                break;

            case TOP_RIGHT:

                cropRect.right += dx;
                cropRect.top += dy;
                break;

            case LEFT:

                cropRect.left += dx;
                break;

            case RIGHT:

                cropRect.right += dx;
                break;

            case BOTTOM_LEFT:

                cropRect.left += dx;
                cropRect.bottom += dy;
                break;

            case BOTTOM:

                cropRect.bottom += dy;
                break;

            case BOTTOM_RIGHT:

                cropRect.right += dx;
                cropRect.bottom += dy;
                break;

            default:
                return;
        }

        fixMinSize();

        keepInsideBitmap();

    }
    private void fixMinSize() {

        if (cropRect.width() < MIN_CROP_SIZE) {

            switch (activeHandle) {

                case TOP_LEFT:
                case LEFT:
                case BOTTOM_LEFT:

                    cropRect.left = cropRect.right - MIN_CROP_SIZE;
                    break;

                default:

                    cropRect.right = cropRect.left + MIN_CROP_SIZE;
                    break;
            }
        }

        if (cropRect.height() < MIN_CROP_SIZE) {

            switch (activeHandle) {

                case TOP_LEFT:
                case TOP:
                case TOP_RIGHT:

                    cropRect.top = cropRect.bottom - MIN_CROP_SIZE;
                    break;

                default:

                    cropRect.bottom = cropRect.top + MIN_CROP_SIZE;
                    break;
            }
        }

    }

    private void keepInsideBitmap() {

        if (cropRect.left < bitmapRect.left)
            cropRect.left = bitmapRect.left;

        if (cropRect.top < bitmapRect.top)
            cropRect.top = bitmapRect.top;

        if (cropRect.right > bitmapRect.right)
            cropRect.right = bitmapRect.right;

        if (cropRect.bottom > bitmapRect.bottom)
            cropRect.bottom = bitmapRect.bottom;

    }
    public Rect getBitmapCropRect() {

        float scaleX = bitmap.getWidth() / bitmapRect.width();

        float scaleY = bitmap.getHeight() / bitmapRect.height();

        int left = Math.round((cropRect.left - bitmapRect.left) * scaleX);

        int top = Math.round((cropRect.top - bitmapRect.top) * scaleY);

        int right = Math.round((cropRect.right - bitmapRect.left) * scaleX);

        int bottom = Math.round((cropRect.bottom - bitmapRect.top) * scaleY);

        return new Rect(left, top, right, bottom);
    }

    public void resetCrop() {

        float margin = 60f;

        cropRect.set(
                bitmapRect.left + margin,
                bitmapRect.top + margin,
                bitmapRect.right - margin,
                bitmapRect.bottom - margin
        );

        invalidate();

        if (listener != null) {
            listener.onCropChanged(getBitmapCropRect());
            listener.isCropChanged(false);
        }

    }

    public interface OnCropChangedListener{

        void onCropChanged(Rect bitmapRect);
        void  isCropChanged(boolean isChanged);

    }

}
