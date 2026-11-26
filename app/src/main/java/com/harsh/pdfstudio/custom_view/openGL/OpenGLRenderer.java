package com.harsh.pdfstudio.custom_view.openGL;

import android.graphics.Color;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import androidx.core.graphics.ColorUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/*
 * Production fixes applied to the original version:
 *
 * 1. BUG: the selector's aspect-ratio uniform was being set *after* the draw calls that
 *    depend on it, so the correction was always one frame stale (and undefined - GL
 *    uniforms default to 0 - on the very first frame, causing a squashed/invisible
 *    selector dot on load). It is now set before any draw call that uses it.
 *
 * 2. PERF: the selector circle geometry (border + fill) was rebuilt with a fresh
 *    ByteBuffer.allocateDirect() on *every single frame* (2 direct-buffer allocations
 *    x 60fps). Direct buffers are expensive to allocate/GC. The geometry never changes
 *    (only its screen offset does, via a uniform), so it is now built once in
 *    onSurfaceCreated() and reused.
 *
 * 3. BUG: `uAspectRatio`'s uniform location was queried with glGetUniformLocation()
 *    every frame instead of once. Also removed a stray glUniform2f() call that reused
 *    a uniform location obtained from selectorProgram while `program` (a different,
 *    unrelated GL program) was bound - uniform locations are not portable across
 *    programs. The main gradient quad never needs to move, so the dead
 *    `uSelectorOffset` uniform was removed from its vertex shader entirely.
 *
 * 4. THREADING: hue/selectorX/selectorY/selectedColor are written from the UI thread
 *    (via SaturationGLView) and read from the GL thread (onDrawFrame). They are now
 *    `volatile` so the GL thread reliably observes updates, and each frame snapshots
 *    them once so the border and fill draw calls use a consistent value.
 */

import android.graphics.Color;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import androidx.core.graphics.ColorUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private static final float SELECTOR_RADIUS = 0.09f;
    private static final float SELECTOR_BORDER_RADIUS = 0.11f;
    private static final int SELECTOR_SEGMENTS = 64;

    private static final int POSITION_COMPONENTS = 2;
    private static final int UV_COMPONENTS = 2;
    private static final int FLOAT_SIZE = 4;
    private static final int STRIDE = (POSITION_COMPONENTS + UV_COMPONENTS) * FLOAT_SIZE;

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "attribute vec2 aUV;\n" +
                    "varying vec2 vUV;\n" +
                    "void main() {\n" +
                    "    gl_Position = aPosition;\n" +
                    "    vUV = aUV;\n" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 vUV;\n" +
                    "uniform float uHue;\n" +
                    "\n" +
                    "vec3 hsv2rgb(vec3 c) {\n" +
                    "    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);\n" +
                    "    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
                    "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
                    "}\n" +
                    "\n" +
                    "void main() {\n" +
                    "    float saturation = vUV.x;\n" +
                    "    float value = pow(1.0 - vUV.y, 0.5);\n" +
                    "    vec3 rgb = hsv2rgb(vec3(uHue, saturation, value));\n" +
                    "    gl_FragColor = vec4(rgb, 1.0);\n" +
                    "}";

    private static final String SELECTOR_VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "uniform vec2 uOffset;\n" +
                    "uniform float uAspectRatio;\n" +
                    "void main() {\n" +
                    "    vec4 position = aPosition;\n" +
                    "    position.x /= uAspectRatio;\n" +
                    "    position.xy += uOffset;\n" +
                    "    gl_Position = position;\n" +
                    "}";

    private static final String SELECTOR_FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "uniform vec3 uSelectorColor;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = vec4(uSelectorColor, 1.0);\n" +
                    "}";

    private final float[] vertices = {
            // x     y     u     v
            -1f, -1f,  0f,  1f,
            1f, -1f,  1f,  1f,
            -1f,  1f,  0f,  0f,
            1f,  1f,  1f,  0f
    };

    // Gradient quad program
    private int program;
    private int positionHandle;
    private int uvHandle;
    private int hueHandle;
    private FloatBuffer vertexBuffer;

    // Selector (indicator dot) program
    private int selectorProgram;
    private int selectorPositionHandle;
    private int selectorColorHandle;
    private int selectorOffsetHandle;
    private int aspectRatioHandle;
    private FloatBuffer selectorFillBuffer;
    private FloatBuffer selectorBorderBuffer;
    private int selectorVertexCount;

    private float aspectRatio = 1f;

    // Shared, cross-thread state - written from the UI thread, read on the GL thread.
    private volatile float hue = 0f;
    private volatile float selectorX = 1f;
    private volatile float selectorY = 1f;
    private volatile int selectedColor = Color.WHITE;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        GLES20.glClearColor(0f, 0f, 0f, 1f);

        program = linkProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        createVertexBuffer();

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        uvHandle = GLES20.glGetAttribLocation(program, "aUV");
        hueHandle = GLES20.glGetUniformLocation(program, "uHue");

        selectorProgram = linkProgram(SELECTOR_VERTEX_SHADER, SELECTOR_FRAGMENT_SHADER);

        selectorPositionHandle = GLES20.glGetAttribLocation(selectorProgram, "aPosition");
        selectorOffsetHandle = GLES20.glGetUniformLocation(selectorProgram, "uOffset");
        selectorColorHandle = GLES20.glGetUniformLocation(selectorProgram, "uSelectorColor");
        aspectRatioHandle = GLES20.glGetUniformLocation(selectorProgram, "uAspectRatio");

        // The circle geometry itself never changes - only its on-screen offset does
        // (via uOffset). Build it once instead of every frame.
        selectorVertexCount = SELECTOR_SEGMENTS + 2;
        selectorBorderBuffer = createSelectorCircle(SELECTOR_BORDER_RADIUS);
        selectorFillBuffer = createSelectorCircle(SELECTOR_RADIUS);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        aspectRatio = height == 0 ? 1f : (float) width / (float) height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawGradientQuad();
        drawSelector();
    }

    private void drawGradientQuad() {

        GLES20.glUseProgram(program);
        GLES20.glUniform1f(hueHandle, hue);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, POSITION_COMPONENTS, GLES20.GL_FLOAT, false, STRIDE, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        vertexBuffer.position(POSITION_COMPONENTS);
        GLES20.glVertexAttribPointer(uvHandle, UV_COMPONENTS, GLES20.GL_FLOAT, false, STRIDE, vertexBuffer);
        GLES20.glEnableVertexAttribArray(uvHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);
    }

    private void drawSelector() {

        // Snapshot once so the border and fill for this frame agree with each other.
        float x = selectorX;
        float y = selectorY;
        int color = selectedColor;

        GLES20.glUseProgram(selectorProgram);

        // Must happen before the draw calls below - this is the uniform the shader
        // uses to correct the circle for non-square viewports.
        GLES20.glUniform1f(aspectRatioHandle, aspectRatio);
        GLES20.glUniform2f(selectorOffsetHandle, x, y);

        GLES20.glEnableVertexAttribArray(selectorPositionHandle);

        // Border ring: black on light colors, white on dark colors, for contrast.
        selectorBorderBuffer.position(0);
        GLES20.glVertexAttribPointer(selectorPositionHandle, 2, GLES20.GL_FLOAT, false, 2 * FLOAT_SIZE, selectorBorderBuffer);

        if (ColorUtils.calculateLuminance(color) > 0.5) {
            GLES20.glUniform3f(selectorColorHandle, 0f, 0f, 0f);
        } else {
            GLES20.glUniform3f(selectorColorHandle, 1f, 1f, 1f);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, selectorVertexCount);

        // Fill: the actual selected color.
        selectorFillBuffer.position(0);
        GLES20.glVertexAttribPointer(selectorPositionHandle, 2, GLES20.GL_FLOAT, false, 2 * FLOAT_SIZE, selectorFillBuffer);

        GLES20.glUniform3f(
                selectorColorHandle,
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f
        );

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, selectorVertexCount);

        GLES20.glDisableVertexAttribArray(selectorPositionHandle);
    }

    private int linkProgram(String vertexSource, String fragmentSource) {

        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);

        int programId = GLES20.glCreateProgram();
        GLES20.glAttachShader(programId, vertexShader);
        GLES20.glAttachShader(programId, fragmentShader);
        GLES20.glLinkProgram(programId);

        int[] status = new int[1];
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, status, 0);

        if (status[0] == 0) {
            String log = GLES20.glGetProgramInfoLog(programId);
            GLES20.glDeleteProgram(programId);
            throw new RuntimeException("Program link failed:\n" + log);
        }

        // Shaders are retained by the program once linked; safe to drop our references.
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        return programId;
    }

    private int compileShader(int type, String source) {

        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);

        if (status[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile failed:\n" + log);
        }

        return shader;
    }

    private void createVertexBuffer() {

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    private FloatBuffer createSelectorCircle(float radius) {

        float[] circleVertices = new float[(SELECTOR_SEGMENTS + 2) * 2];

        circleVertices[0] = 0f;
        circleVertices[1] = 0f;

        for (int i = 0; i <= SELECTOR_SEGMENTS; i++) {
            float angle = (float) (2.0 * Math.PI * i / SELECTOR_SEGMENTS);
            circleVertices[(i + 1) * 2] = (float) Math.cos(angle) * radius;
            circleVertices[(i + 1) * 2 + 1] = (float) Math.sin(angle) * radius;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(circleVertices.length * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer floatBuffer = bb.asFloatBuffer();
        floatBuffer.put(circleVertices);
        floatBuffer.position(0);

        return floatBuffer;
    }

    /** @param hueDegrees hue in the 0-360 range. */
    public void setHue(float hueDegrees) {
        hue = hueDegrees / 360f;
    }

    /** @param x,y clip-space (-1..1) position of the selector indicator. */
    public void setSelectorPosition(float x, float y) {
        selectorX = Math.max(-1f, Math.min(x, 1f));
        selectorY = Math.max(-1f, Math.min(y, 1f));
    }

    public void setSelectedColor(int color) {
        selectedColor = color;
    }
}