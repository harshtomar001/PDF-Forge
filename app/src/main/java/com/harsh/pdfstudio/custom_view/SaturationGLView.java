package com.harsh.pdfstudio.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import com.harsh.pdfstudio.custom_view.openGL.OpenGLRenderer;


/*
 * Production fixes applied to the original version:
 *
 * 1. DEAD CODE: this class overrode View.onDraw(Canvas) to paint the hue/value
 *    gradients and drew a selector circle with a Paint. GLSurfaceView punches a hole
 *    in the window for its GL Surface (composited separately by SurfaceFlinger), so
 *    Canvas drawing done in onDraw() on a SurfaceView is not visible under normal
 *    z-ordering - it was silently doing wasted work (rebuilding a LinearGradient
 *    shader on every size change, drawing on every invalidate()) on top of the actual
 *    OpenGL rendering that already draws the same gradient and selector on the GPU.
 *    Removed entirely, along with the now-unused huePaint/valuePaint/selectorFillPaint
 *    fields and drawHSVPaint().
 *
 * 2. BUG: requestDisallowInterceptTouchEvent(true) was set on ACTION_DOWN but never
 *    released - same parent-scroll-lock bug as HueSliderView. There was also no
 *    ACTION_UP/ACTION_CANCEL handling at all, and no performClick() override despite
 *    consuming touch events (an accessibility requirement). All fixed.
 *
 * 3. BUG: setHSV(hue, saturation, value) converted `value` to a GL y-offset with a
 *    plain linear mapping (`value * 2 - 1`), but the fragment shader (and the
 *    touch-handling code) both apply a sqrt curve to value for a more perceptually
 *    even gradient (value = pow(1 - uv.y, 0.5)). That mismatch meant that whenever a
 *    color was set *programmatically* (typing RGB/hex, tapping a recent color) rather
 *    than by dragging, the little selector dot would land in the wrong spot on the
 *    gradient - visually next to the wrong shade. The math is now inverted correctly
 *    (glY = 2*value^2 - 1) so the dot always matches the actual displayed gradient.
 *
 * 4. Added divide-by-zero guards for touches/layout that happen before the view has a
 *    real size, and getHSV() now returns a defensive copy instead of the live internal
 *    array.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import com.harsh.pdfstudio.custom_view.openGL.OpenGLRenderer;

public class SaturationGLView extends GLSurfaceView {

    // View-space bounds, used only for clamping/normalizing touch coordinates.
    private final RectF colorRect = new RectF();

    private OpenGLRenderer renderer;

    private float hue = 0f;
    private float saturation = 1f;
    private float value = 1f;
    private int selectedColor;

    private final float[] hsv = new float[3];

    public interface OnColorChangedListener {
        void onColorChanged(int color);
        void getHex(String hex);
    }

    private OnColorChangedListener listener;

    public void setOnColorChangedListener(OnColorChangedListener listener) {

        this.listener = listener;

    }

    public SaturationGLView(Context context) {
        super(context);
        init();
    }

    public SaturationGLView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SaturationGLView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        // GLSurfaceView has no 3-arg constructor; defStyleAttr is intentionally unused.
        super(context, attrs);
        init();
    }

    private void init() {

        setEGLContextClientVersion(2);

        renderer = new OpenGLRenderer();

        hsv[0] = hue;
        hsv[1] = saturation;
        hsv[2] = value;
        selectedColor = Color.HSVToColor(hsv);

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        colorRect.set(0, 0, w, h);

        // Now that we have real bounds, make sure the GPU selector indicator agrees
        // with whatever hue/saturation/value this view currently represents.
        syncSelectorPosition();
    }

    public void setHue(float hue) {

        this.hue = hue;
        hsv[0] = hue;
        selectedColor = Color.HSVToColor(hsv);

        renderer.setSelectedColor(selectedColor);
        renderer.setHue(hue);
        requestRender();

        notifyListener();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                // fall through - also react to the initial touch-down position
            case MotionEvent.ACTION_MOVE:
                handleTouch(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_UP:
                handleTouch(event.getX(), event.getY());
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

    private void handleTouch(float x, float y) {

        if (colorRect.width() <= 0 || colorRect.height() <= 0) return;

        float clampedX = Math.max(colorRect.left, Math.min(x, colorRect.right));

        float clampedY = Math.max(colorRect.top, Math.min(y, colorRect.bottom));

        saturation = (clampedX - colorRect.left) / colorRect.width();

        float linearValue = 1f - ((clampedY - colorRect.top) / colorRect.height());
        value = (float) Math.pow(linearValue, 0.5); // perceptual curve, matches the shader

        hsv[1] = saturation;
        hsv[2] = value;
        selectedColor = Color.HSVToColor(hsv);

        // glY = linearValue * 2 - 1 is algebraically equal to 2*value^2 - 1 here since
        // value = sqrt(linearValue) - kept in the "un-curved" form because it's simpler
        // and avoids a redundant sqrt/square round trip during drag.
        float glX = saturation * 2f - 1f;
        float glY = linearValue * 2f - 1f;

        renderer.setSelectorPosition(glX, glY);
        renderer.setSelectedColor(selectedColor);
        requestRender();

        notifyListener();
    }

    /** Sets the full HSV state, e.g. from a hex/RGB entry or a recent-color tap. */
    public void setHSV(float hue, float saturation, float value) {

        this.hue = hue;
        this.saturation = saturation;
        this.value = value;

        hsv[0] = hue;
        hsv[1] = saturation;
        hsv[2] = value;
        selectedColor = Color.HSVToColor(hsv);

        renderer.setHue(hue);
        renderer.setSelectedColor(selectedColor);
        syncSelectorPosition();
    }

    private void syncSelectorPosition() {

        float glX = saturation * 2f - 1f;
        // Inverse of the shader's value = pow(1 - uv.y, 0.5) curve.
        float glY = 2f * value * value - 1f;

        renderer.setSelectorPosition(glX, glY);

        requestRender();
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onColorChanged(selectedColor);
            listener.getHex(getSelectedHex());
        }
    }

    /** @return a defensive copy - callers must not mutate the view's internal HSV state. */
    public float[] getHSV() {
        return hsv.clone();
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public String getSelectedHex() {
        return String.format("#%06X", (0xFFFFFF & selectedColor));
    }
}