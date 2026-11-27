package com.harsh.pdfstudio.custom_view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import androidx.annotation.Nullable;
import com.harsh.pdfstudio.model.ImageObject;
import com.harsh.pdfstudio.model.RotatedBounds;
import com.harsh.pdfstudio.R;
import java.util.ArrayList;


public class AddImageView extends View {

    private Bitmap pageBitmap;

    private final RectF pageBounds = new RectF();

    private ImageObject activeImage=null;

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint activeHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint handleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int canvasBGColor = Color.parseColor("#f2f5f4");

    private static final float DEFAULT_IMAGE_SCALE = 0.5f;

    private static final float HANDLE_RADIUS = 18f;

    private static final float DELETE_RADIUS = 25f;

    private static final float HANDLE_RADIUS_CIRCLE=55f;

    private static final int SELECTION_COLOR = Color.parseColor("#2962FF");

    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private  final  Paint deletePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private  final  Paint crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int MIN_WIDTH = 120;

    private ArrayList<ImageObject> imageList = new ArrayList<>();

    private ScaleGestureDetector scaleDetector;

    private float previousRotationAngle;

    private float lastTouchX;

    private float lastTouchY;

    private boolean isDragging = false;

    private enum TouchMode {
        NONE,
        DRAG,
        RESIZE,
        TRANSFORM
    }

    private enum Handle {
        NONE,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        DELETE
    }

    TouchMode touchMode = TouchMode.NONE;
    Handle activeHandle = Handle.NONE;

    private OnImageSelectionListener selectionListener;

    public void setOnImageSelectionListener(OnImageSelectionListener listener) {
        this.selectionListener = listener;
    }


    public AddImageView(Context context){

        super(context);

        init();

    }

    public AddImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AddImageView(Context context,
                        @Nullable AttributeSet attrs,
                        int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init(){

        canvasBGColor = androidx.core.content.ContextCompat.getColor(getContext(), R.color.color_surface_variant);

        borderPaint.setColor(Color.GRAY);
        borderPaint.setStrokeWidth(3f);
        borderPaint.setStyle(Paint.Style.STROKE);

        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);

        handleStrokePaint.setColor(Color.parseColor("#2962FF"));
        handleStrokePaint.setStrokeWidth(3f);
        handleStrokePaint.setStyle(Paint.Style.STROKE);


        bitmapPaint.setColor(Color.WHITE);

        shadowPaint.setColor(Color.WHITE);
        shadowPaint.setShadowLayer(
                12f,
                0f,
                4f,
                Color.argb(70, 0, 0, 0)
        );

        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint);

        scaleDetector = new ScaleGestureDetector(
                getContext(),
                new ScaleListener()
        );

        activeHandlePaint.setStyle(Paint.Style.FILL);
        activeHandlePaint.setColor(Color.parseColor("#2962FF"));

        deletePaint.setColor(Color.BLACK);

        crossPaint.setColor(Color.WHITE);
        crossPaint.setStrokeWidth(4f);


    }

    public void setPageBitmap(Bitmap bitmap){
        if (bitmap == null)
            return;

        this.pageBitmap=bitmap;

        calculatePageBounds();

        invalidate();

    }

    public void setImageBitmap(Bitmap bitmap,long id){

        if (bitmap == null) return;

        ImageObject image = new ImageObject();

        image.setId(id);

        image.setBitmap(bitmap);

        image.setSelected(true);


        // Deselect all previous images
        for (ImageObject img : imageList) {
            img.setSelected(false);
        }

        if(pageBounds.isEmpty()){
            calculatePageBounds();
        }

        if(pageBitmap != null){

            float aspectRatio = bitmap.getWidth() / (float) bitmap.getHeight();

            image.setAspectRation(aspectRatio);


            float initialWidth = pageBounds.width() * DEFAULT_IMAGE_SCALE;

            float initialHeight = pageBounds.height() * DEFAULT_IMAGE_SCALE;

            float scale = Math.min(initialWidth / bitmap.getWidth(), initialHeight / bitmap.getHeight());

            float drawWidth = bitmap.getWidth() * scale;

            float drawHeight = bitmap.getHeight() * scale;

            float left = pageBounds.centerX() - drawWidth / 2f;

            float top = pageBounds.centerY() - drawHeight / 2f;

            image.setBounds(left, top, left + drawWidth, top + drawHeight);


        }

        activeImage = image;

        imageList.add(image);

        notifySelectionChanged();

        invalidate();


    }

    @Override
    protected void onDraw(Canvas canvas){

        super.onDraw(canvas);

        canvas.drawColor(canvasBGColor);

        canvas.drawRect(pageBounds, shadowPaint);

        if(pageBitmap!=null){

            canvas.drawBitmap(pageBitmap,null,pageBounds,null);
        }


        for(ImageObject image:imageList){

            if (image != null && image.getBitmap() != null) {

                canvas.save();

                RectF bounds = image.getBounds();

                float cx = bounds.centerX();
                float cy = bounds.centerY();

                canvas.rotate(image.getRotation(), cx, cy);

                float sx = image.isFlipHorizontal() ? -1f : 1f;
                float sy = image.isFlipVertical() ? -1f : 1f;

                canvas.scale(sx, sy, cx, cy);


                canvas.drawBitmap(
                        image.getBitmap(),
                        null,
                        bounds,
                        bitmapPaint
                );


                if(image.isSelected()){

                    drawHandles(canvas,image);

                    borderPaint.setColor(SELECTION_COLOR);

                    canvas.drawRect(
                            image.getBounds(),
                            borderPaint
                    );

                    float deleteX = bounds.centerX();

                    float deleteY = bounds.top - 50f;

                    canvas.drawCircle(deleteX, deleteY, DELETE_RADIUS, deletePaint);

                    drawDeleteBtn(canvas, deleteX , deleteY);

                }
                else borderPaint.setColor(Color.GRAY);

                canvas.restore();




            }

        }




    }

    private void  calculatePageBounds(){

        if(pageBitmap==null || getWidth()== 0 || getHeight() == 0) return;

        float viewWidth = getWidth();

        float viewHeight = getHeight();

        float pageBitmapHeight = pageBitmap.getHeight();

        float pageBitmapWidth = pageBitmap.getWidth();

        float scale = Math.min(viewWidth / pageBitmapWidth,
                viewHeight / pageBitmapHeight);

        float drawBitmapWidth = pageBitmapWidth * scale;

        float drawBitmapHeight = pageBitmapHeight * scale;

        float left = (viewWidth - drawBitmapWidth) / 2f;

        float top = (viewHeight - drawBitmapHeight) / 2f;

        pageBounds.set( left, top , left + drawBitmapWidth , top + drawBitmapHeight );





    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){

        calculatePageBounds();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (imageList == null)
            return false;

        float x = event.getX();
        float y = event.getY();

        scaleDetector.onTouchEvent(event);

        if(event.getPointerCount()>2) {

            isDragging = false;
            return true;

        }


        switch(event.getActionMasked()) {

            case MotionEvent.ACTION_POINTER_DOWN:

                if (activeImage == null) {

                    float midX = (event.getX(0) + event.getX(1)) / 2f;
                    float midY = (event.getY(0) + event.getY(1)) / 2f;

                    activeImage = findTouchedImage(midX, midY);

                    if (activeImage == null)
                        return true;

                    for (ImageObject img : imageList)
                        img.setSelected(false);

                    activeImage.setSelected(true);
                    notifySelectionChanged();
                }

                touchMode = TouchMode.TRANSFORM;
                isDragging = false;
                notifySelectionChanged();

                previousRotationAngle = getRotationAngle(event);

                invalidate();

                return true;

            case MotionEvent.ACTION_DOWN:


                // 1. First check handle of active image
                Handle touchedHandle;

                if (activeImage != null) {

                    touchedHandle = getTouchedHandle(x, y, activeImage);

                    if(touchedHandle == Handle.DELETE){

                        imageList.remove(activeImage);
                        activeImage = null;
                        invalidate();
                        notifySelectionChanged();
                        return true;

                    }

                    else if (touchedHandle != Handle.NONE) {

                        touchMode = TouchMode.RESIZE;
                        activeHandle = touchedHandle;

                        isDragging = true;

                        lastTouchX = x;
                        lastTouchY = y;

                        invalidate();

                        return true;
                    }
                }

                // 2. Check which image was clicked
                ImageObject touched = findTouchedImage(x, y);

                if (touched != null) {

                    for (ImageObject img : imageList)
                        img.setSelected(false);

                    activeImage = touched;

                    activeImage.setSelected(true);

                    notifySelectionChanged();

                    touchMode = TouchMode.DRAG;
                    activeHandle = Handle.NONE;

                    isDragging = true;

                    lastTouchX = x;
                    lastTouchY = y;

                    invalidate();

                    return true;
                }

                // 3. Clicked empty area
                for (ImageObject img : imageList)
                    img.setSelected(false);

                activeImage = null;

                notifySelectionChanged();

                invalidate();

                break;

            case MotionEvent.ACTION_MOVE:

                if (touchMode == TouchMode.TRANSFORM) {

                    float currentAngle = getRotationAngle(event);

                    float delta = currentAngle - previousRotationAngle;

                    float newRotation = activeImage.getRotation() + delta;

                    activeImage.setRotation(newRotation);

                    previousRotationAngle = currentAngle;

                    keepRotatedImageInsidePage(activeImage);


                    invalidate();

                    return true;
                }

                else  if (isDragging) {

                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    switch (touchMode) {

                        case DRAG:

                            if( activeImage != null)

                                moveImage(activeImage,dx, dy);

                            break;

                        case RESIZE:

                            if(activeImage != null && activeHandle !=Handle.NONE )

                                resizeImage(activeImage ,x, y, activeHandle);

                            break;
                    }

                    lastTouchX = x;
                    lastTouchY = y;

                    invalidate();

                    return true;
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                isDragging = false;

                touchMode = TouchMode.NONE;

                activeHandle = Handle.NONE;

                invalidate();

                return true;

            case MotionEvent.ACTION_POINTER_UP:

                touchMode = TouchMode.NONE;

                return true;


            default:

        }

        return true;
    }

    private void drawHandles(Canvas canvas,ImageObject image) {

        RectF r = image.getBounds();

        drawHandle(canvas, r.left, r.top, Handle.TOP_LEFT);

        drawHandle(canvas, r.right, r.top, Handle.TOP_RIGHT);

        drawHandle(canvas, r.left, r.bottom, Handle.BOTTOM_LEFT);

        drawHandle(canvas, r.right, r.bottom, Handle.BOTTOM_RIGHT);
    }
    
    private  void  drawDeleteBtn(Canvas canvas , float deleteX , float deleteY){

        float r = DELETE_RADIUS * 0.5f;

        canvas.drawLine(
                deleteX - r,
                deleteY - r,
                deleteX + r,
                deleteY + r,
                crossPaint
        );

        canvas.drawLine(
                deleteX - r,
                deleteY + r,
                deleteX + r,
                deleteY - r,
                crossPaint
        );

    }

    private ImageObject findTouchedImage(float x, float y) {

        for (int i = imageList.size() - 1; i >= 0; i--) {

            ImageObject img = imageList.get(i);

            if (img.getBounds().contains(x, y)) {
                return img;
            }
        }

        return null;
    }

    private void drawHandle(Canvas canvas, float cx, float cy, Handle handle) {

        Paint fillPaint =
                (activeHandle == handle)
                        ? activeHandlePaint
                        : handlePaint;

        float radius =
                activeHandle == handle
                        ? HANDLE_RADIUS + 3f
                        : HANDLE_RADIUS;

        // Fill
        canvas.drawCircle(cx, cy, radius, fillPaint);

        // Border
        canvas.drawCircle(cx, cy, radius, handleStrokePaint);
    }

    private void moveImage(ImageObject image, float dx, float dy) {

        RectF bounds = image.getBounds();

        bounds.offset(dx, dy);

        keepImageInsidePage(image);



    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float scaleFactor = detector.getScaleFactor();

            scaleImage(scaleFactor);

            invalidate();

            return true;
        }
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            isDragging = false;

            return true;
        }

    }

    private void scaleImage(float scaleFactor ) {

        float MAX_WIDTH = pageBounds.width() * 0.98f;
        float MAX_HEIGHT = pageBounds.height() * 0.98f;

        if(activeImage ==  null)return;


        RectF bounds = activeImage.getBounds();


        float newWidth = bounds.width() * scaleFactor;

        // Clamp width
        newWidth = Math.max(newWidth, MIN_WIDTH);
        newWidth = Math.min(newWidth, MAX_WIDTH);

        // Compute height from aspect ratio
        float newHeight = newWidth / activeImage.getAspectRatio();

        float widthScale = MAX_WIDTH / newWidth;
        float heightScale = MAX_HEIGHT / newHeight;

        float fitScale = Math.min(1f,
                Math.min(widthScale, heightScale));

        newWidth *= fitScale;
        newHeight *= fitScale;



        float dw = (newWidth - bounds.width()) / 2f;
        float dh = (newHeight - bounds.height()) / 2f;

        bounds.inset(-dw, -dh);

        if (bounds.left < pageBounds.left) {

            bounds.offset(pageBounds.left - bounds.left, 0);

        }

        if (bounds.right > pageBounds.right) {

            bounds.offset(pageBounds.right - bounds.right, 0);

        }

        if (bounds.top < pageBounds.top) {

            bounds.offset(0, pageBounds.top - bounds.top);

        }

        if (bounds.bottom > pageBounds.bottom) {

            bounds.offset(0, pageBounds.bottom - bounds.bottom);

        }

    }

    private Handle getTouchedHandle(float x, float y, ImageObject activeImage) {

        RectF r = activeImage.getBounds();

        RotatedBounds rb = getRotatedBounds(activeImage);


        if (isHandleTouched(x,y, rb.deletePoint.x,rb.deletePoint.y))
            return  Handle.DELETE;

        if (isHandleTouched(x, y, r.left, r.top))
            return Handle.TOP_LEFT;

        if (isHandleTouched(x, y, r.right, r.top))
            return Handle.TOP_RIGHT;

        if (isHandleTouched(x, y, r.left, r.bottom))
            return Handle.BOTTOM_LEFT;

        if (isHandleTouched(x, y, r.right, r.bottom))
            return Handle.BOTTOM_RIGHT;

        return Handle.NONE;
    }

    private boolean isHandleTouched(float touchX, float touchY, float handleX, float handleY) {

        float dx = touchX - handleX;
        float dy = touchY - handleY;

        return dx * dx + dy * dy <= HANDLE_RADIUS_CIRCLE * HANDLE_RADIUS_CIRCLE;
    }

    private void resizeImage(ImageObject  activeImage,float fingerX, float fingerY, Handle handle) {

        float MAX_WIDTH =pageBounds.width() * 0.98f;

        float MAX_HEIGHT = pageBounds.height() * 0.98f;

        RectF bounds = activeImage.getBounds();


        switch (handle) {

            case BOTTOM_RIGHT: {

                float newWidth = fingerX - bounds.left;

                newWidth = clampWidth(newWidth);

                // Keep aspect ratio
                float newHeight = newWidth / activeImage.getAspectRatio();

                // limit the height
                if (newHeight > MAX_HEIGHT) {

                    newHeight = MAX_HEIGHT;

                    newWidth = newHeight * activeImage.getAspectRatio();

                }

                // Bottom-right moves
                bounds.right = bounds.left + newWidth;

                bounds.bottom = bounds.top + newHeight;

                // Keep inside page
                if (bounds.right > pageBounds.right) {

                    bounds.right = pageBounds.right;

                    newWidth = bounds.width();

                    newHeight = newWidth / activeImage.getAspectRatio();

                    bounds.bottom = bounds.top + newHeight;
                }

                if (bounds.bottom > pageBounds.bottom) {

                    bounds.bottom = pageBounds.bottom;

                    newHeight = bounds.height();

                    newWidth = newHeight * activeImage.getAspectRatio();

                    bounds.right = bounds.left + newWidth;
                }

                break;
            }

            case BOTTOM_LEFT: {

                float newWidth = bounds.right - fingerX;

                newWidth = clampWidth(newWidth);

                // Keep aspect ratio
                float newHeight = newWidth / activeImage.getAspectRatio();

                // limit the height
                if (newHeight > MAX_HEIGHT) {

                    newHeight = MAX_HEIGHT;

                    newWidth = newHeight * activeImage.getAspectRatio();

                }

                bounds.left = bounds.right - newWidth;

                bounds.bottom = bounds.top + newHeight;

                // Keep inside page
                if (bounds.left < pageBounds.left) {

                    bounds.left = pageBounds.left;

                    newWidth = bounds.width();

                    newHeight = newWidth / activeImage.getAspectRatio();

                    bounds.bottom = bounds.top + newHeight;
                }

                if (bounds.bottom > pageBounds.bottom) {

                    bounds.bottom = pageBounds.bottom;

                    newHeight = bounds.height();

                    newWidth = newHeight * activeImage.getAspectRatio();

                    bounds.left = bounds.right - newWidth;
                }

                break;
            }

            case TOP_LEFT: {

                float newWidth =  bounds.right - fingerX;

                newWidth = clampWidth(newWidth);
                // Keep aspect ratio
                 float newHeight = newWidth / activeImage.getAspectRatio();

                // limit the height
                if (newHeight > MAX_HEIGHT) {

                    newHeight = MAX_HEIGHT;

                    newWidth = newHeight * activeImage.getAspectRatio();

                }

                bounds.left = bounds.right - newWidth;

                bounds.top  = bounds.bottom - newHeight;

                // Keep inside page
                if (bounds.left < pageBounds.left) {

                    bounds.left = pageBounds.left;

                    newWidth = bounds.width();

                    newHeight = newWidth / activeImage.getAspectRatio();

                    bounds.top = bounds.bottom - newHeight;
                }

                if (bounds.top < pageBounds.top) {

                    bounds.top = pageBounds.top;

                    newHeight = bounds.height();

                    newWidth = newHeight * activeImage.getAspectRatio();

                    bounds.left = bounds.right - newWidth;
                }

                break;
        }

            case TOP_RIGHT: {

                float newWidth = fingerX - bounds.left;

                // Minimum width
                newWidth = Math.max(newWidth, MIN_WIDTH);

                // Maximum width
                newWidth = Math.min(newWidth, MAX_WIDTH);

                // Keep aspect ratio
                 float newHeight = newWidth / activeImage.getAspectRatio();

                // limit the height
                if (newHeight > MAX_HEIGHT) {

                    newHeight = MAX_HEIGHT;

                    newWidth = newHeight * activeImage.getAspectRatio();

                }

                // Bottom-right moves
                bounds.right = bounds.left + newWidth;

                bounds.top = bounds.bottom - newHeight;

                // Keep inside page
                if (bounds.right > pageBounds.right) {

                    bounds.right = pageBounds.right;

                    newWidth = bounds.width();

                    newHeight = newWidth / activeImage.getAspectRatio();

                    bounds.top = bounds.bottom - newHeight;
                }

                if (bounds.top < pageBounds.top) {

                    bounds.top = pageBounds.top;

                    newHeight = bounds.height();

                    newWidth = newHeight * activeImage.getAspectRatio();

                    bounds.right = bounds.left + newWidth;
                }

                break;
            }


            default:
                break;

        }

        invalidate();
    }

    private float clampWidth(float newWidth){

        float MAX_WIDTH = pageBounds.width() * 0.98f;
        newWidth = Math.max(newWidth, MIN_WIDTH);

        // Maximum width
        newWidth = Math.min(newWidth, MAX_WIDTH);

        return  newWidth;
    }

    public   boolean isImgSelected(){

        return  activeImage != null ? true : false;

    }

    public void  setReplaceBitmap(Bitmap bitmap){

        if(activeImage == null) return;

        activeImage.setBitmap(bitmap);

        float aspectRatio = bitmap.getWidth() / (float) bitmap.getHeight();

        activeImage.setAspectRation(aspectRatio);

        RectF bounds = activeImage.getBounds();

        // Keep current width
        float width = bounds.width();

        // Calculate new height
        float newHeight = width / aspectRatio;

        // Keep image center fixed
        float cx = bounds.centerX();
        float cy = bounds.centerY();

        bounds.left = cx - width / 2f;
        bounds.right = cx + width / 2f;

        bounds.top = cy - newHeight / 2f;
        bounds.bottom = cy + newHeight / 2f;

        if (bounds.left < pageBounds.left) {

            bounds.offset(pageBounds.left - bounds.left, 0);

        }

        if (bounds.right > pageBounds.right) {

            bounds.offset(pageBounds.right - bounds.right, 0);

        }

        if (bounds.top < pageBounds.top) {

            bounds.offset(0, pageBounds.top - bounds.top);

        }

        if (bounds.bottom > pageBounds.bottom) {

            bounds.offset(0, pageBounds.bottom - bounds.bottom);

        }

        invalidate();


    }

    private float getRotationAngle(MotionEvent event) {

        if (event.getPointerCount() < 2)
            return 0f;

        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);

        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    private RotatedBounds getRotatedBounds(ImageObject image) {

        RectF b = image.getBounds();

        float cx = b.centerX();
        float cy = b.centerY();

        float angle = image.getRotation();

        RotatedBounds rb = new RotatedBounds();

        rb.topLeft =
                rotatePoint(b.left, b.top, cx, cy, angle);

        rb.topRight =
                rotatePoint(b.right, b.top, cx, cy, angle);

        rb.bottomLeft =
                rotatePoint(b.left, b.bottom, cx, cy, angle);

        rb.bottomRight =
                rotatePoint(b.right, b.bottom, cx, cy, angle);

        rb.minX = Math.min(
                Math.min(rb.topLeft.x, rb.topRight.x),
                Math.min(rb.bottomLeft.x, rb.bottomRight.x)
        );

        rb.maxX = Math.max(
                Math.max(rb.topLeft.x, rb.topRight.x),
                Math.max(rb.bottomLeft.x, rb.bottomRight.x)
        );

        rb.minY = Math.min(
                Math.min(rb.topLeft.y, rb.topRight.y),
                Math.min(rb.bottomLeft.y, rb.bottomRight.y)
        );

        rb.maxY = Math.max(
                Math.max(rb.topLeft.y, rb.topRight.y),
                Math.max(rb.bottomLeft.y, rb.bottomRight.y)
        );

        rb.boundingRect = new RectF(
                rb.minX,
                rb.minY,
                rb.maxX,
                rb.maxY
        );

        rb.deletePoint = rotatePoint(
                b.centerX(),
                b.top - 50f,
                b.centerX(),
                b.centerY(),
                image.getRotation()
        );

        return rb;
    }

    private void keepImageInsidePage(ImageObject image) {

        RotatedBounds rb = getRotatedBounds(image);

        float dx = 0;
        float dy = 0;

        if (rb.minX < pageBounds.left)
            dx += pageBounds.left - rb.minX;

        if (rb.maxX > pageBounds.right)
            dx += pageBounds.right - rb.maxX;

        if (rb.minY < pageBounds.top)
            dy += pageBounds.top - rb.minY;

        if (rb.maxY > pageBounds.bottom)
            dy += pageBounds.bottom - rb.maxY;

        image.getBounds().offset(dx, dy);
    }

    private PointF rotatePoint(float x, float y, float cx, float cy, float angle) {

        double rad = Math.toRadians(angle);

        float dx = x - cx;
        float dy = y - cy;

        float rx = (float) (
                dx * Math.cos(rad) -
                        dy * Math.sin(rad)
        );

        float ry = (float) (
                dx * Math.sin(rad) +
                        dy * Math.cos(rad)
        );

        return new PointF(
                cx + rx,
                cy + ry
        );
    }

    private void keepRotatedImageInsidePage(ImageObject image) {

        moveRotatedImageInside(image);

        if (!isRotatedImageInside(image)) {

            shrinkImage(image, 0.99f);

            keepRotatedImageInsidePage(image);
        }
    }

    private boolean isRotatedImageInside(ImageObject image) {

        RotatedBounds rb = getRotatedBounds(image);

        return pageBounds.contains(rb.boundingRect);
    }

    private void moveRotatedImageInside(ImageObject image) {

        RotatedBounds rb = getRotatedBounds(image);

        float dx = 0;
        float dy = 0;

        if (rb.minX < pageBounds.left)
            dx = pageBounds.left - rb.minX;

        else if (rb.maxX > pageBounds.right)
            dx = pageBounds.right - rb.maxX;

        if (rb.minY < pageBounds.top)
            dy = pageBounds.top - rb.minY;

        else if (rb.maxY > pageBounds.bottom)
            dy = pageBounds.bottom - rb.maxY;

        image.getBounds().offset(dx, dy);
    }

    private void shrinkImage(ImageObject image, float factor) {

        RectF b = image.getBounds();

        float cx = b.centerX();
        float cy = b.centerY();

        float newWidth = b.width() * factor;
        float newHeight = b.height() * factor;

        b.left = cx - newWidth / 2f;
        b.right = cx + newWidth / 2f;

        b.top = cy - newHeight / 2f;
        b.bottom = cy + newHeight / 2f;
    }

    public void flipHorizontal() {

        if (activeImage == null)
            return;

        activeImage.setFlipHorizontal(
                !activeImage.isFlipHorizontal()
        );

        invalidate();
    }

    public void flipVertical() {

        if (activeImage == null)
            return;

        activeImage.setFlipVertical(
                !activeImage.isFlipVertical()
        );

        invalidate();
    }

    private void notifySelectionChanged() {

        if (selectionListener != null) {
            selectionListener.onImageSelected(activeImage);
        }
    }

    public  void  clearTrash(){

        setPageBitmap(null);
        imageList.clear();
        invalidate();

    }

    public ImageObject getActiveImage(){

        return  activeImage;

    }


    //  it convert the image bounds which are relative to the view bitmap  to pdf page bitmap bounds.
    public RectF convertViewToPdfBounds(RectF viewBounds, float scaleFactor) {

        float offsetX = pageBounds.left;
        float offsetY = pageBounds.top;

        return new RectF(
                (viewBounds.left - offsetX) * scaleFactor,
                (viewBounds.top - offsetY) * scaleFactor,
                (viewBounds.right - offsetX) * scaleFactor,
                (viewBounds.bottom - offsetY) * scaleFactor
        );
    }

    public  ArrayList<ImageObject> getImageList(){
        return imageList;
    }

    public interface OnImageSelectionListener {
        void onImageSelected(@Nullable ImageObject image);
    }


}
