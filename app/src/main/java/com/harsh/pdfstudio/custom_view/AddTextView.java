package com.harsh.pdfstudio.custom_view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.managers.FontManager;
import com.harsh.pdfstudio.model.AddTextObject;
import com.harsh.pdfstudio.model.FontResult;
import com.harsh.pdfstudio.utils.Dp_to_Px;
import com.harsh.pdfstudio.utils.Vibrate;
import java.util.ArrayList;
import java.util.List;

public class AddTextView extends View {


    public interface addTextObjectListener {
        void  onTextSizeChanged(float newSize);

        void objectSelected(AddTextObject object);

        void  onPencilClick(AddTextObject object);

        void  onTouchOutsidePencil(boolean isInsideSameTextBox);


    }

    private addTextObjectListener listener;

    private final  int ORIGINAL_TEXT_SIZE = Dp_to_Px.dpToPx(24);

    private  final  float ORIGINAL_TEXT_ROTATION =  0f;

    private  final  float ORIGINAL_TEXT_OPACITY  = 1f;

    private  final  int  ORIGINAL_TEXT_COLOR = Color.BLUE;

    private  final  String ORIGINAL_TEXT = "Type Here...";

    public void setAddTextObjectListener(addTextObjectListener listener) {

        this.listener = listener;

    }

    private final List<AddTextObject> textObjects = new ArrayList<>();

    private AddTextObject selectedTextObject;

    private final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint removeButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint removeIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float REMOVE_BUTTON_MARGIN = 20f;
    private  final  float REMOVE_BUTTON_VIRTUAL_RADIUS =  40f;
    private static final float REMOVE_BUTTON_RADIUS = 22f;

    private  static final  float PENCIL_BUTTON_RADIUS =  22f;

    private  static  final  float PENCIL_BUTTON_MARGIN =  20f;

    private  static  final  float PENCIL_BUTTON_VIRTUAL_RADIUS =  40f;

    private final Matrix inverseMatrix = new Matrix();

    private ScaleGestureDetector scaleDetector;

    private float lastAngle;
    private boolean twoFingerGesture = false;

    private static final float MIN_TEXT_SIZE = 10f;
    private static final float MAX_TEXT_SIZE = 300f;
    private static final float TEXT_TOUCH_MARGIN = 60f;

    private float lastTouchX;
    private float lastTouchY;

    private Paint textPaint;
    private Paint borderPaint;

    private Bitmap pageBitmap;

    private float feedbackOffsetX = 0f;

    private RectF pageBounds = new RectF();
    private boolean isSelected;

    private float gestureStartTextSize;
    private float gestureStartScale = 1f;

    private  AddTextObject prevSelectedObject =   null;

    private static FontManager fontManager;


    public AddTextView(Context context) {
        super(context);
        init();
    }


    public AddTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint.setColor(Color.BLUE);

        textPaint.setTextSize(60);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        borderPaint.setColor(Color.BLUE);

        borderPaint.setStyle(Paint.Style.STROKE);

        borderPaint.setStrokeWidth(3);

        guidePaint.setColor(Color.parseColor("#4A90E2"));
        guidePaint.setStrokeWidth(2.5f);

        guidePaint.setStyle(Paint.Style.STROKE);

        guidePaint.setPathEffect(new DashPathEffect(new float[]{12,12}, 0));

        removeButtonPaint.setColor(Color.RED);
        removeButtonPaint.setStyle(Paint.Style.FILL);

        removeIconPaint.setColor(Color.WHITE);
        removeIconPaint.setStyle(Paint.Style.STROKE);
        removeIconPaint.setStrokeWidth(4f);
        removeIconPaint.setStrokeCap(Paint.Cap.ROUND);


        fontManager = new FontManager(getContext());

        initGestureDetector(getContext());

    }

    public void setPageBitmap(Bitmap pageBitmap) {

        this.pageBitmap = pageBitmap;
        calculatePageBounds();
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (pageBitmap == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        canvas.drawBitmap(
                this.pageBitmap,
                null,
                this.pageBounds,
                null
        );

        if (textObjects.isEmpty()) {
            return;
        }

        for (AddTextObject object : textObjects) {

            FontResult fontResult = fontManager.getFont(object.getFontFamily(),
                                                        object.isBold() ? 700 : object.getFontWeight(),
                                                        object.isItalic()
            );

            textPaint.setTypeface(fontResult.getTypeface());

            if( fontResult.weightFallback &&
                    !object.isWeightFallbackWarningShown()){

                showFontFallbackMessage("Selected weight unavailable. Switched to Regular.");

                object.setWeightFallbackWarningShown(true);

            }

            if(fontResult.fontFallback &&
                    !object.isFontFallbackWarningShown()){

                showFontFallbackMessage("Font unavailable. Switched to Default.");

                object.setFontFallbackWarningShown(true);

            }


            // Text size
            textPaint.setTextSize(object.getTextSize());

            // Text color
            textPaint.setColor(object.getTextColor());

            // Opacity
            textPaint.setAlpha((int) (object.getOpacity() * 255));

            // Underline
            textPaint.setUnderlineText(object.isUnderline());

            // Strike-through
            textPaint.setStrikeThruText(object.isStrikeThrough());

            canvas.save();

            canvas.rotate(
                    object.getRotation(),
                    object.getBounds().centerX(),
                    object.getBounds().centerY()
            );

            RectF bounds = object.getBounds();

            Paint.FontMetrics fm = textPaint.getFontMetrics();

            float textWidth = textPaint.measureText(object.getText());

            float baseline = bounds.centerY() - (fm.ascent + fm.descent) / 2f;

            float lineX = bounds.centerX() - textWidth / 2f;

            // Draw text
            canvas.drawText(
                    object.getText(),
                    lineX,
                    baseline,
                    textPaint
            );

            // Selection UI
            if (object == selectedTextObject) {

                canvas.drawRect(bounds, borderPaint);

                drawRemoveButton(canvas, bounds, (int) object.getTextSize());

                drawEditButton(canvas, bounds, (int) object.getTextSize());

            }

            canvas.restore();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);

        calculatePageBounds();

        for (AddTextObject object : textObjects) {

            if (!object.isPositioned()) {
                calculateBounds(object);
            }
        }

        invalidate();

    }

    private void drawRemoveButton(Canvas canvas, RectF bounds, int textSize) {

        float scale = getHelperScale(textSize);

        float buttonRadius = REMOVE_BUTTON_RADIUS * scale;

        float gap = Math.max(1.5f, 2f * scale); //  so that there is atleast some distance maintain  from the textBox

        float cx = bounds.right + ( 2 * REMOVE_BUTTON_RADIUS * scale) ;
        float cy = bounds.top - buttonRadius - gap;


        canvas.drawCircle(
                cx,
                cy,
                REMOVE_BUTTON_RADIUS * scale,
                removeButtonPaint
        );

        float iconSize = 8f * scale ;

        canvas.drawLine(
                cx - iconSize,
                cy - iconSize,
                cx + iconSize,
                cy + iconSize,
                removeIconPaint
        );

        canvas.drawLine(
                cx + iconSize,
                cy - iconSize,
                cx - iconSize,
                cy + iconSize,
                removeIconPaint
        );

    }

    private void drawEditButton(Canvas canvas, RectF bounds, int textSize) {

        float scale = getHelperScale(textSize);

        float buttonRadius = PENCIL_BUTTON_RADIUS * scale;

        // Top-center of textbox
        float centerX = bounds.centerX();
        float centerY = bounds.top - (2 * PENCIL_BUTTON_MARGIN * scale);

        canvas.save();

        // -----------------------------
        // Button circle
        // -----------------------------

        Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setColor(Color.WHITE);

        canvas.drawCircle(centerX, centerY, buttonRadius, buttonPaint);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(Color.rgb(40, 100, 230));

        canvas.drawCircle(centerX, centerY, buttonRadius, borderPaint);

        // -----------------------------
        // Pencil icon — filled silhouette, 45° diagonal
        // (tip, body, eraser cap, built along the pencil's own axis)
        // -----------------------------

        int pencilColor = Color.rgb(40, 100, 230);

        // Unit vectors: axis = along the pencil (tip -> eraser), perp = across its width
        float axisX = 0.7071f, axisY = -0.7071f;
        float perpX = 0.7071f, perpY = 0.7071f;

        float tipLen    = 7f   * scale;  // sharp graphite point
        float bodyLen   = 20f  * scale;  // wooden body
        float capLen    = 6f   * scale;  // eraser cap
        float halfW     = 4.4f * scale;  // body half-width
        float capHalfW  = 5f   * scale;  // eraser is slightly wider than the body

        float totalLen = tipLen + bodyLen + capLen;
        float tipX = centerX - axisX * (totalLen / 2f);
        float tipY = centerY - axisY * (totalLen / 2f);

        // --- Tip (filled triangle) ---
        Paint tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tipPaint.setStyle(Paint.Style.FILL);
        tipPaint.setColor(pencilColor);

        Path tip = new Path();
        tip.moveTo(tipX, tipY);
        tip.lineTo(tipX + axisX * tipLen - perpX * halfW, tipY + axisY * tipLen - perpY * halfW);
        tip.lineTo(tipX + axisX * tipLen + perpX * halfW, tipY + axisY * tipLen + perpY * halfW);
        tip.close();
        canvas.drawPath(tip, tipPaint);

        // --- Body (outlined rectangle) ---
        float bodyStart = tipLen;
        float bodyEnd = tipLen + bodyLen;

        Path body = new Path();
        body.moveTo(tipX + axisX * bodyStart - perpX * halfW, tipY + axisY * bodyStart - perpY * halfW);
        body.lineTo(tipX + axisX * bodyEnd - perpX * halfW, tipY + axisY * bodyEnd - perpY * halfW);
        body.lineTo(tipX + axisX * bodyEnd + perpX * halfW, tipY + axisY * bodyEnd + perpY * halfW);
        body.lineTo(tipX + axisX * bodyStart + perpX * halfW, tipY + axisY * bodyStart + perpY * halfW);
        body.close();

        Paint bodyFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyFill.setStyle(Paint.Style.FILL);
        bodyFill.setColor(Color.WHITE);
        canvas.drawPath(body, bodyFill);

        Paint bodyOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyOutline.setStyle(Paint.Style.STROKE);
        bodyOutline.setStrokeWidth(1.6f * scale);
        bodyOutline.setStrokeJoin(Paint.Join.ROUND);
        bodyOutline.setColor(pencilColor);
        canvas.drawPath(body, bodyOutline);

        // --- Eraser cap (filled, slightly wider) ---
        float capStart = bodyEnd;
        float capEnd = bodyEnd + capLen;

        Path cap = new Path();
        cap.moveTo(tipX + axisX * capStart - perpX * capHalfW, tipY + axisY * capStart - perpY * capHalfW);
        cap.lineTo(tipX + axisX * capEnd - perpX * capHalfW, tipY + axisY * capEnd - perpY * capHalfW);
        cap.lineTo(tipX + axisX * capEnd + perpX * capHalfW, tipY + axisY * capEnd + perpY * capHalfW);
        cap.lineTo(tipX + axisX * capStart + perpX * capHalfW, tipY + axisY * capStart + perpY * capHalfW);
        cap.close();

        Paint capPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capPaint.setStyle(Paint.Style.FILL);
        capPaint.setColor(pencilColor);
        canvas.drawPath(cap, capPaint);

        canvas.restore();
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

        this.pageBounds.set( left, top , left + drawBitmapWidth , top + drawBitmapHeight );


    }

    private boolean isTouchInsidePencilBtn(float x, float y, AddTextObject object) {

        if (object == null ||
                object.getBounds() == null) {
            return false;
        }

        RectF bounds = object.getBounds();

        inverseMatrix.reset();

        inverseMatrix.setRotate(
                -object.getRotation(),
                bounds.centerX(),
                bounds.centerY()
        );

        float[] point = {
                x,
                y
        };

        inverseMatrix.mapPoints(point);

        float localX = point[0];
        float localY = point[1];

        float textSize =  object.getTextSize();

        float scale = getHelperScale(textSize);


        float cx = bounds.centerX();

        float cy =
                bounds.top
                        -  ( 2 * PENCIL_BUTTON_MARGIN * scale );

        float dx = localX - cx;
        float dy = localY - cy;

        float radius =
                PENCIL_BUTTON_RADIUS * scale
                        + PENCIL_BUTTON_VIRTUAL_RADIUS * scale ;

        return dx * dx + dy * dy <= radius * radius;
    }

    private  boolean isTouchInsideRemoveBtn(float x , float y , AddTextObject object){

        if(object ==  null || object.getBounds() == null) return false;


        RectF bounds = object.getBounds();

        inverseMatrix.reset();

        inverseMatrix.setRotate(
                -object.getRotation(),
                bounds.centerX(),
                bounds.centerY()
        );

        float[] point = {
                x,
                y
        };

        inverseMatrix.mapPoints(point);

        float localX = point[0];
        float localY = point[1];

        float textSize =  object.getTextSize();
        float scale = getHelperScale(textSize);

        float buttonRadius = REMOVE_BUTTON_RADIUS * scale;
        float gap = Math.max(1.5f, 2f * scale);

        float cx = bounds.right + ( 2 * buttonRadius) ;
        float cy = bounds.top - buttonRadius - gap;

        float dx = localX - cx;
        float dy = localY - cy;


        float radius =
                REMOVE_BUTTON_RADIUS * scale
                        + REMOVE_BUTTON_VIRTUAL_RADIUS * scale ;

        return dx * dx + dy * dy <= radius * radius;


    }

    private boolean selectClosestText(float x, float y) {

        AddTextObject closestObject = null;

        float closestDistance = Float.MAX_VALUE;

        for (int i = textObjects.size() - 1; i >= 0; i--) {

            AddTextObject object = textObjects.get(i);

            RectF bounds = object.getBounds();

            if (bounds == null) continue;


            inverseMatrix.reset();

            inverseMatrix.setRotate(
                    -object.getRotation(),
                    bounds.centerX(),
                    bounds.centerY()
            );

            float[] point = {
                    x,
                    y
            };

            inverseMatrix.mapPoints(point);

            float localX = point[0];
            float localY = point[1];

            /*
             * Actual distance from the textbox.
             */
            float distance = distanceToRect(
                    localX,
                    localY,
                    bounds
            );

            /*
             * Allow clicking slightly outside the textbox.
             */
            if (distance > TEXT_TOUCH_MARGIN) {
                continue;
            }

            /*
             * Smaller distance = better match.
             */
            if (distance < closestDistance) {

                closestDistance = distance;
                closestObject = object;
            }
        }

        selectedTextObject = closestObject;

        if(closestObject != null){
            new Vibrate().vibrate(getContext(), 150);

            listener.objectSelected(closestObject);


        }

        return closestObject != null;
    }

    private float distanceToRect(float x, float y, RectF rect) {

        float dx = 0f;
        float dy = 0f;

        if (x < rect.left) {
            dx = rect.left - x;
        } else if (x > rect.right) {
            dx = x - rect.right;
        }

        if (y < rect.top) {
            dy = rect.top - y;
        } else if (y > rect.bottom) {
            dy = y - rect.bottom;
        }

        return (float) Math.hypot(dx, dy);
    }

    private void calculateBounds(AddTextObject object) {

        if (object == null || pageBitmap == null) {
            return;
        }

        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        calculatePageBounds();

        if (pageBounds.width() <= 0 || pageBounds.height() <= 0) {
            return;
        }


        if(object.getFontFamily() !=  null) textPaint.setTypeface(fontManager.getFont(
                                                    object.getFontFamily(),
                                                    object.isBold() ? 700 : object.getFontWeight(),
                                                    object.isItalic()
                                            ).getTypeface()
        );

        textPaint.setTextSize(object.getTextSize());
        textPaint.setColor(object.getTextColor());

        Paint.FontMetrics fm = textPaint.getFontMetrics();


        String text = object.getText();

        if (text == null) {
            text = "";
        }

        float textWidth = textPaint.measureText(text);

        float textHeight = fm.descent - fm.ascent;


        final float PAGE_MARGIN = 20f;

        float minX = pageBounds.left + PAGE_MARGIN;
        float maxX = pageBounds.right - PAGE_MARGIN;

        float minY = pageBounds.top + PAGE_MARGIN;
        float maxY = pageBounds.bottom - PAGE_MARGIN;


        RectF bounds = object.getBounds();


        if (!object.isPositioned()) {

            float centerX = pageBounds.centerX();
            float centerY = pageBounds.centerY();

            bounds = new RectF(
                    centerX - textWidth / 2f,
                    centerY - textHeight / 2f,
                    centerX + textWidth / 2f,
                    centerY + textHeight / 2f
            );

            object.setBounds(bounds);

            return;
        }


        float centerX = bounds.centerX();
        float centerY = bounds.centerY();

        float halfWidth = textWidth / 2f;
        float halfHeight = textHeight / 2f;

        float left = centerX - halfWidth;
        float right = centerX + halfWidth;

        float top = centerY - halfHeight;
        float bottom = centerY + halfHeight;


        // Horizontal constraint
        if (textWidth <= pageBounds.width() - 2 * PAGE_MARGIN) {

            if (left < minX) {
                float shift = minX - left;

                left += shift;
                right += shift;
            }

            if (right > maxX) {
                float shift = right - maxX;

                left -= shift;
                right -= shift;
            }
        }

        // Vertical constraint
        if (textHeight <= pageBounds.height() - 2 * PAGE_MARGIN) {

            if (top < minY) {
                float shift = minY - top;

                top += shift;
                bottom += shift;
            }

            if (bottom > maxY) {
                float shift = bottom - maxY;

                top -= shift;
                bottom -= shift;
            }
        }


        bounds.set(
                left,
                top,
                right,
                bottom
        );

        object.setBounds(bounds);
    }

    private boolean canResizeTextObject(AddTextObject object, float newTextSize) {

        if (object == null ||
                object.getBounds() == null) {
            return false;
        }

        Paint measurePaint = new Paint(textPaint);

        measurePaint.setTypeface(
                fontManager.getFont(object.getFontFamily(),
                        object.isBold() ? 700 : object.getFontWeight(),
                        object.isItalic()
                        ).getTypeface()
        );

        measurePaint.setTextSize(newTextSize);

        float newWidth =
                measurePaint.measureText(
                        object.getText()
                );

        Paint.FontMetrics fm =
                measurePaint.getFontMetrics();

        float newHeight =
                fm.descent - fm.ascent;

        RectF oldBounds =
                object.getBounds();

        float cx = oldBounds.centerX();
        float cy = oldBounds.centerY();

        RectF newBounds = new RectF(
                cx - newWidth / 2f,
                cy - newHeight / 2f,
                cx + newWidth / 2f,
                cy + newHeight / 2f
        );

        RectF rotatedBounds =
                getRotatedBounds(
                        newBounds,
                        object.getRotation()
                );

        float margin = 1f;

        return rotatedBounds.left >= pageBounds.left - margin
                && rotatedBounds.top >= pageBounds.top - margin
                && rotatedBounds.right <= pageBounds.right + margin
                && rotatedBounds.bottom <= pageBounds.bottom + margin;
    }

    public float getLargestFittingTextSize(AddTextObject object, float requestedSize) {

        if (object == null) {
            return MIN_TEXT_SIZE;
        }

        float currentSize =
                object.getTextSize();

        requestedSize = Math.max(
                MIN_TEXT_SIZE,
                Math.min(MAX_TEXT_SIZE, requestedSize)
        );

        // Shrinking is always allowed
        if (requestedSize <= currentSize) {
            return requestedSize;
        }

        // Requested size already fits
        if (canResizeTextObject(object, requestedSize)) {
            return requestedSize;
        }

        float low = currentSize;
        float high = requestedSize;

        // Binary search for largest fitting size
        for (int i = 0; i < 20; i++) {

            float middle =
                    (low + high) / 2f;

            if (canResizeTextObject(object, middle)) {

                low = middle;

            } else {

                high = middle;
            }
        }

        return low;
    }

    public float getLargestFittingTextSizeBetween(AddTextObject object, float minSize, float maxSize) {

        if (object == null) {
            return minSize;
        }

        float low = minSize;
        float high = maxSize;

        for (int i = 0; i < 20; i++) {

            float middle =
                    (low + high) / 2f;

            if (canResizeTextObject(
                    object,
                    middle
            )) {

                // This size fits.
                // Try going larger.
                low = middle;

            } else {

                // Doesn't fit.
                // Go smaller.
                high = middle;
            }
        }

        return low;
    }

    private RectF getRotatedBounds(RectF rect, float rotation) {

        float cx = rect.centerX();
        float cy = rect.centerY();

        float[] points = {
                rect.left,  rect.top,
                rect.right, rect.top,
                rect.right, rect.bottom,
                rect.left,  rect.bottom
        };

        Matrix matrix = new Matrix();

        matrix.setRotate(rotation, cx, cy);
        matrix.mapPoints(points);

        float minX = points[0];
        float maxX = points[0];
        float minY = points[1];
        float maxY = points[1];

        for (int i = 2; i < points.length; i += 2) {

            minX = Math.min(minX, points[i]);
            maxX = Math.max(maxX, points[i]);

            minY = Math.min(minY, points[i + 1]);
            maxY = Math.max(maxY, points[i + 1]);
        }

        return new RectF(
                minX,
                minY,
                maxX,
                maxY
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (textObjects.isEmpty())
            return false;

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {

                float x = event.getX();
                float y = event.getY();


                if(selectedTextObject != null && isTouchInsideRemoveBtn(x,y,selectedTextObject)){

                    removeTextObject(selectedTextObject);

                    selectedTextObject = null;
                    isSelected = false;
                    twoFingerGesture = false;

                    invalidate();
                    return true;

                }



                if (selectedTextObject != null &&
                        isTouchInsidePencilBtn(x, y, selectedTextObject)) {

                    AddTextObject objectToEdit = selectedTextObject;

                    if(objectToEdit != null){

                    listener.onPencilClick(objectToEdit);

                        lastTouchX = x;
                        lastTouchY = y;
                        lastAngle = getAngle(event);


                    }

                    invalidate();

                    return true;
                }



                prevSelectedObject  = getSelectedTextObject();


                isSelected = selectClosestText(x, y);


                if (isSelected && selectedTextObject != null) {
                    lastTouchX = x;
                    lastTouchY = y;
                    lastAngle = getAngle(event);

                    if(prevSelectedObject  == getSelectedTextObject()) listener.onTouchOutsidePencil(true);

                     else listener.onTouchOutsidePencil(false);

                    Log.d("listener inside tb" , true+"");

                }

                else {

                    listener.onTouchOutsidePencil(false);

                    Log.d("listener outside tb" , false+"");


                }


                // Feed the detector too so its internal state starts clean.
                scaleDetector.onTouchEvent(event);

                invalidate();

                return true;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {

                if (event.getPointerCount() == 2) {

                    if (selectedTextObject == null) {

                        float midX = (event.getX(0) + event.getX(1)) / 2f;
                        float midY = (event.getY(0) + event.getY(1)) / 2f;

                        isSelected = selectClosestText(midX, midY);
                    }

                    if (selectedTextObject != null) {

                        lastAngle = getAngle(event);

                        twoFingerGesture = true;
                    }

                    invalidate();
                }

                // IMPORTANT: run this AFTER selection is resolved above,
                // otherwise onScaleBegin() sees selectedTextObject == null
                // and permanently disables scaling for this gesture.
                scaleDetector.onTouchEvent(event);

                return true;
            }

            case MotionEvent.ACTION_MOVE: {

                if (selectedTextObject == null) return true;

                // This was missing — without it onScale() never fires.
                scaleDetector.onTouchEvent(event);

                if (event.getPointerCount() >= 2) {

                    float currentAngle = getAngle(event);

                    float rotationDelta = normalizeAngle(currentAngle - lastAngle);

                    float currentRotation = selectedTextObject.getRotation();

                    float requestedRotation = currentRotation + rotationDelta;

                    requestedRotation = normalizeAngle(requestedRotation);

                    float validRotation =
                            findValidRotation(
                                    selectedTextObject,
                                    currentRotation,
                                    requestedRotation
                            );

                    selectedTextObject.setRotation(validRotation);

                    lastAngle = currentAngle;

                    invalidate();

                    return true;
                }

                if (!twoFingerGesture && isSelected) {

                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;


                    constrainToPage(selectedTextObject,dx ,dy);

                    selectedTextObject.setPositioned(true);

                    lastTouchX = event.getX();
                    lastTouchY = event.getY();

                    invalidate();
                }

                return true;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                scaleDetector.onTouchEvent(event);

                if (event.getPointerCount() == 2) {

                    twoFingerGesture = false;

                    int index = event.getActionIndex() == 0 ? 1 : 0;

                    if (index < event.getPointerCount()) {
                        lastTouchX = event.getX(index);
                        lastTouchY = event.getY(index);
                    }
                }

                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {

                scaleDetector.onTouchEvent(event);

                twoFingerGesture = false;

                invalidate();

                return true;
            }
        }

        return true;
    }

    public AddTextObject addNewText() {

        AddTextObject object = new AddTextObject(ORIGINAL_TEXT,
                                                 ORIGINAL_TEXT_SIZE,
                                                 ORIGINAL_TEXT_COLOR,
                                                 ORIGINAL_TEXT_OPACITY
                                                 );


        object.setHint(true);

        textObjects.add(object);

        object.setFontFallbackWarningShown(false);

        object.setWeightFallbackWarningShown(false);

        selectedTextObject = object;

//        listener.objectSelected(selectedTextObject);

        calculateBounds(object);

        invalidate();

        return object;
    }

    public void updateTextObject(AddTextObject object) {

        if (object == null) {
            return;
        }

        FontResult fontResult = fontManager.getFont(
                object.getFontFamily(),
                object.isBold() ? 700 : object.getFontWeight(),
                object.isItalic()
        );

        Typeface typeface = fontResult.getTypeface();

        if( fontResult.weightFallback &&
                !object.isWeightFallbackWarningShown()){

            showFontFallbackMessage("Selected weight unavailable. Switched to Regular.");

            object.setWeightFallbackWarningShown(true);

        }

        if(fontResult.fontFallback &&
                !object.isFontFallbackWarningShown()){

            showFontFallbackMessage("Font unavailable. Switched to Default.");

            object.setFontFallbackWarningShown(true);

        }


        textPaint.setTypeface(typeface);

        textPaint.setTextSize(
                object.getTextSize()
        );

        textPaint.setColor(
                object.getTextColor()
        );

        textPaint.setAlpha(
                (int) (object.getOpacity() * 255f)
        );

        textPaint.setUnderlineText(
                object.isUnderline()
        );

        textPaint.setStrikeThruText(
                object.isStrikeThrough()
        );

        calculateBounds(object);

        invalidate();
    }

    public  AddTextObject getSelectedTextObject(){
        return selectedTextObject;
    }

    public void  removeTextObject(AddTextObject object){

        if(textObjects.contains(object)){

            textObjects.remove(object);

            invalidate();

        }


    }

    public void resetTextObject(AddTextObject object) {

        if (object == null) return;

        final float DEFAULT_SIZE = Dp_to_Px.dpToPx(24);

        final float MIN_SIZE = MIN_TEXT_SIZE;

        float currentSize = object.getTextSize();

        float finalSize;


        object.setRotation(0f);


        if (canResizeTextObject(object, DEFAULT_SIZE)) {

            finalSize = DEFAULT_SIZE;

            Log.d(
                    "RESET",
                    "24dp fits → using 24dp"
            );
        }


        else if (canResizeTextObject(
                object,
                MIN_SIZE
        )) {

            finalSize =
                    getLargestFittingTextSizeBetween(
                            object,
                            MIN_SIZE,
                            DEFAULT_SIZE
                    );

            Log.d(
                    "RESET",
                    "24dp doesn't fit → using "
                            + finalSize
            );
        }


        else {

            finalSize = currentSize;

            Log.d(
                    "RESET",
                    "Even 10dp doesn't fit → keeping "
                            + currentSize
            );
        }

        object.setTextSize(finalSize);

        // Reset rotation


        // Recalculate bounds
        calculateBounds(object);

        RectF bounds = object.getBounds();

        if (bounds == null) return;

        // Center the object
        float dx = pageBounds.centerX() - bounds.centerX();

        float dy = pageBounds.centerY() - bounds.centerY();

        bounds.offset(dx, dy);

        invalidate();
    }

    private void initGestureDetector(Context context) {

        scaleDetector = new ScaleGestureDetector(
                context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {

                        if (selectedTextObject == null) return false;

                        gestureStartTextSize = selectedTextObject.getTextSize();

                        gestureStartScale = 1f;

                        twoFingerGesture = true;

                        return true;
                    }

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {

                        if (selectedTextObject == null) {
                            return false;
                        }

                        gestureStartScale *= detector.getScaleFactor();

                        float requestedSize = gestureStartTextSize * gestureStartScale;

                        requestedSize = Math.max(
                                MIN_TEXT_SIZE,
                                Math.min(MAX_TEXT_SIZE, requestedSize)
                        );

                        float validSize = getLargestFittingTextSize(selectedTextObject, requestedSize);

                        listener.onTextSizeChanged(validSize);

                        selectedTextObject.setTextSize(validSize);

                        calculateBounds(selectedTextObject);

                        invalidate();

                        return true;
                    }

                    @Override
                    public void onScaleEnd(
                            ScaleGestureDetector detector) {

                        twoFingerGesture = false;
                    }
                }
        );
    }

    private float getAngle(MotionEvent event) {

        if (event.getPointerCount() < 2) return 0f;

        float dx = event.getX(1) - event.getX(0);

        float dy = event.getY(1) - event.getY(0);

        return (float) Math.toDegrees(
                Math.atan2(dy, dx)
        );
    }

    private boolean isRotationInsidePage(AddTextObject object, float proposedRotation) {

        RectF bounds = object.getBounds();

        float cx = bounds.centerX();
        float cy = bounds.centerY();

        double radians = Math.toRadians(proposedRotation);

        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        // Original four corners
        float[][] points = {
                {bounds.left,  bounds.top},
                {bounds.right, bounds.top},
                {bounds.right, bounds.bottom},
                {bounds.left,  bounds.bottom}
        };

        for (float[] point : points) {

            float x = point[0] - cx;
            float y = point[1] - cy;

            // Rotate around center
            float rotatedX =
                    x * cos - y * sin + cx;

            float rotatedY =
                    x * sin + y * cos + cy;

            // Check against page
            if (rotatedX < pageBounds.left ||
                    rotatedX > pageBounds.right ||
                    rotatedY < pageBounds.top ||
                    rotatedY > pageBounds.bottom) {

                return false;
            }
        }

        return true;
    }

    private float findValidRotation(AddTextObject object, float currentRotation, float requestedRotation) {

        // Requested rotation is already valid
        if (isRotationInsidePage(object, requestedRotation)) {
            return requestedRotation;
        }

        float low = currentRotation;
        float high = requestedRotation;

        // 20 iterations is more than enough for UI precision
        for (int i = 0; i < 20; i++) {

            float mid = (low + high) / 2f;

            if (isRotationInsidePage(object, mid)) {

                low = mid;

            } else {

                high = mid;
            }
        }

        return low;
    }

    private float normalizeAngle(float angle) {

        while (angle > 180f) {
            angle -= 360f;
        }

        while (angle < -180f) {
            angle += 360f;
        }

        return angle;
    }

    private void constrainToPage(AddTextObject object, float dx , float dy ) {

        if (object == null || object.getBounds() == null)
            return;

        RectF bounds = object.getBounds();

        // Proposed new position
        RectF newBounds = new RectF(bounds);

        newBounds.offset(dx, dy);

        // Check the rotated textbox
        RectF rotatedBounds = getRotatedBounds(
                newBounds,
                object.getRotation()
        );

        float correctionX = 0f;
        float correctionY = 0f;

        // Left
        if (rotatedBounds.left < pageBounds.left) {
            correctionX = pageBounds.left - rotatedBounds.left;
        }

        // Right
        if (rotatedBounds.right > pageBounds.right) {
            correctionX = pageBounds.right - rotatedBounds.right;
        }

        // Top
        if (rotatedBounds.top < pageBounds.top) {
            correctionY = pageBounds.top - rotatedBounds.top;
        }

        // Bottom
        if (rotatedBounds.bottom > pageBounds.bottom) {
            correctionY = pageBounds.bottom - rotatedBounds.bottom;
        }

        // Apply corrected movement
        newBounds.offset(
                correctionX,
                correctionY
        );

        object.setBounds(newBounds);


    }

    public boolean canFitText(AddTextObject object, String newText) {

        if (newText == null) return false;

        textPaint.setTextSize(object.getTextSize());

        float textWidth = textPaint.measureText(newText);

        float margin = 20f;

        // Current text position
        float centerX = object.getBounds().centerX();

        float left = centerX - textWidth / 2f;
        float right = centerX + textWidth / 2f;

        boolean canFit =  left >= pageBounds.left + margin ||
                right <= pageBounds.right - margin;
        //  there may be case when boundary touches the text but not the another one


        if(!canFit){

            showTextLimitFeedback(getSelectedTextObject());

            if(object.getTextSize() != MIN_TEXT_SIZE ){

                Toast.makeText(
                        getContext(),
                        "Text limit reached. Reduce text size to add more characters.",
                        Toast.LENGTH_SHORT
                ).show();


            }
            else Toast.makeText(getContext(),
                    "Text limit reached. Use another textbox to add more characters",
                    Toast.LENGTH_SHORT
            ).show();

            return canFit;

        }

        return canFit;

    }

    private void showTextLimitFeedback(AddTextObject object) {

        if (object == null) return;

        RectF bounds = object.getBounds();

        final float originalLeft = bounds.left;
        final float originalRight = bounds.right;

        ValueAnimator animator =
                ValueAnimator.ofFloat(
                        0f, 2f, -2f, 2f, -2f, 0f
                );

        animator.setDuration(180);

        animator.addUpdateListener(animation -> {

            float offset =
                    (float) animation.getAnimatedValue() * 4f;

            bounds.left = originalLeft + offset;
            bounds.right = originalRight + offset;

            invalidate();
        });

        // Guarantee exact restoration even if the animator
        // is cancelled/interrupted mid-shake.
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                bounds.left = originalLeft;
                bounds.right = originalRight;
                invalidate();
            }
        });

        animator.start();
    }

    private void showFontFallbackMessage(String message) {

        View root = getRootView();

        Snackbar snackbar = Snackbar.make(
                root,
                message,
                Snackbar.LENGTH_SHORT
        );

        snackbar.setBackgroundTint(
                ContextCompat.getColor(
                        getContext(),
                        R.color.white
                )
        );

        snackbar.setTextColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.black
                )
        );

        snackbar.setTextMaxLines(2);

        View snackbarView = snackbar.getView();

        // Rounded corners
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(
                getResources().getDisplayMetrics().density * 12
        );

        snackbarView.setBackground(background);

        // Small horizontal margins
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams)
                        snackbarView.getLayoutParams();

        int margin = Dp_to_Px.dpToPx(16);

        params.leftMargin = margin;
        params.rightMargin = margin;
        params.bottomMargin = margin;

        snackbarView.setLayoutParams(params);

        snackbar.show();

        // Move Snackbar above keyboard
        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                (v, insets) -> {

                    Insets imeInsets =
                            null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        imeInsets = insets.getInsets(
                                WindowInsetsCompat.Type.ime()
                        ).toPlatformInsets();
                    }

                    Insets systemInsets = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        systemInsets = insets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                        ).toPlatformInsets();

                    }

                    int keyboardHeight = 0;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        keyboardHeight = imeInsets.bottom;

                    }
                    else {

                        keyboardHeight = imeInsets.bottom;

                    }

                    int bottomMargin;

                    if (keyboardHeight > 0) {

                        bottomMargin = keyboardHeight + Dp_to_Px.dpToPx(12);

                    }
                    else {

                        bottomMargin = systemInsets.bottom + Dp_to_Px.dpToPx(16);

                    }

                    ViewGroup.MarginLayoutParams lp =
                            (ViewGroup.MarginLayoutParams)
                                    snackbarView.getLayoutParams();

                    lp.bottomMargin = bottomMargin;

                    snackbarView.setLayoutParams(lp);

                    return insets;
                }
        );
    }


    public boolean changeFont(AddTextObject object, String newFontFamily) {

        if (object == null) {
            return false;
        }

        String oldFontFamily = object.getFontFamily();

        float oldTextSize = object.getTextSize();


        // Temporarily apply new font
        object.setFontFamily(newFontFamily);

        // New font works at current size
        if (canResizeTextObject(
                object,
                oldTextSize
        )) {

            calculateBounds(object);


            invalidate();

            return true;
        }

        // New font doesn't fit at current size,
        // but can fit if we reduce the size
        if (canResizeTextObject(
                object,
                MIN_TEXT_SIZE
        )) {

            float fittingSize =
                    getLargestFittingTextSizeBetween(
                            object,
                            MIN_TEXT_SIZE,
                            oldTextSize
                    );

            object.setTextSize(fittingSize);

            calculateBounds(object);

            invalidate();

            return true;
        }

        // Nothing works -> restore old font
        object.setFontFamily(oldFontFamily);
        object.setTextSize(oldTextSize);

        calculateBounds(object);

        invalidate();

        return false;
    }

    private float getHelperScale(float textSize) {

        // 24dp text → 1.0 scale
        // 18dp text → 0.75 scale
        // 12dp text → 0.50 scale
        // 10dp text → 0.42 scale

        float scale = textSize / ORIGINAL_TEXT_SIZE;

        Log.d("scale",  scale+"");

        return Math.max(0.45f, Math.min(1.0f, scale));
    }

}