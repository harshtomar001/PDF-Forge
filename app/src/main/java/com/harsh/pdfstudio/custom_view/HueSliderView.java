package com.harsh.pdfstudio.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;


/*
 * Production fixes applied to the original version:
 *
 * 1. BUG: requestDisallowInterceptTouchEvent(true) was called on ACTION_DOWN but never
 *    released. Once a user touched this slider even once, the parent (e.g. a
 *    ScrollView/ViewPager the picker lives in) would have touch interception disabled
 *    *forever*, silently breaking scrolling/swiping elsewhere in the screen. Now
 *    released on ACTION_UP/ACTION_CANCEL.
 *
 * 2. Removed the large commented-out "horizontal slider" dead code blocks and the
 *    unused `selectorPositionX` field left over from that variant, to keep the class
 *    readable.
 *
 * 3. Defensive guard against a divide-by-zero in updateHue() if a touch somehow lands
 *    before the view has been laid out.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

public class HueSliderView extends View {

    private static final float CORNER_RADIUS = 40f;
    private static final float SELECTOR_RADIUS = 20f;
    private static final float TRACK_WIDTH = 50f;
    private static final float VERTICAL_MARGIN = 40f;

    private Paint huePaint;
    private Paint selectorFillPaint;
    private Paint selectorStrokePaint;
    private RectF hueRect;

    private final float[] hsv = new float[]{0f, 1f, 1f};

    private float hue = 0f;
    private float selectorPositionY;

    public interface OnHueChangeListener {
        void onHueChanged(float hue);
    }

    private OnHueChangeListener listener;

    public void setOnHueChangeListener(OnHueChangeListener listener) {
        this.listener = listener;
    }

    public HueSliderView(Context context) {
        super(context);
        init();
    }

    public HueSliderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HueSliderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        huePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        selectorFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorFillPaint.setColor(Color.WHITE);

        selectorStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorStrokePaint.setStyle(Paint.Style.STROKE);
        selectorStrokePaint.setStrokeWidth(4);
        selectorStrokePaint.setColor(Color.BLACK);

        hueRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        hueRect.set(
                w / 2f - TRACK_WIDTH / 2f,
                VERTICAL_MARGIN,
                w / 2f + TRACK_WIDTH / 2f,
                h - VERTICAL_MARGIN
        );

        selectorPositionY = hueRect.top + (hue / 360f) * hueRect.height();

        createHueGradient();
    }

    private void createHueGradient() {

        int[] colors = {
                Color.RED,
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.MAGENTA,
                Color.RED
        };

        LinearGradient gradient = new LinearGradient(
                hueRect.centerX(),
                hueRect.top,
                hueRect.centerX(),
                hueRect.bottom,
                colors,
                null,
                Shader.TileMode.CLAMP
        );

        huePaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRoundRect(hueRect, CORNER_RADIUS, CORNER_RADIUS, huePaint);

        float cx = hueRect.centerX();

        canvas.drawCircle(cx, selectorPositionY, SELECTOR_RADIUS, selectorFillPaint);
        canvas.drawCircle(cx, selectorPositionY, SELECTOR_RADIUS, selectorStrokePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                // fall through - also react to the initial touch-down position
            case MotionEvent.ACTION_MOVE:
                updateHue(event.getY());
                return true;

            case MotionEvent.ACTION_UP:
                updateHue(event.getY());
                getParent().requestDisallowInterceptTouchEvent(false);
                performClick();
                return true;

            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void updateHue(float y) {

        if (hueRect.height() <= 0) return;

        selectorPositionY = Math.max(hueRect.top, Math.min(y, hueRect.bottom));

        float percent = (selectorPositionY - hueRect.top) / hueRect.height();
        hue = percent * 360f;

        hsv[0] = hue;
        hsv[1] = 1f;
        hsv[2] = 1f;

        selectorFillPaint.setColor(Color.HSVToColor(hsv));

        if (listener != null) listener.onHueChanged(hue);

        invalidate();
    }

    public float getHue() {
        return hue;
    }

    public int getSelectedColor() {
        return Color.HSVToColor(hsv);
    }

    public void setHue(float hue) {

        this.hue = Math.max(0f, Math.min(hue, 360f));

        if (hueRect.height() > 0) {
            selectorPositionY = hueRect.top + (this.hue / 360f) * hueRect.height();
        }

        hsv[0] = this.hue;
        hsv[1] = 1f;
        hsv[2] = 1f;

        selectorFillPaint.setColor(Color.HSVToColor(hsv));

        invalidate();
    }
}