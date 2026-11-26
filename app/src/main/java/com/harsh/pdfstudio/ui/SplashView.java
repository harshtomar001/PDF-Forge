package com.harsh.pdfstudio.ui;



import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashView extends View {

    // ─── Page dimensions (dp → converted to px in constructor) ───────────────
    private float PW, PH, FOLD;

    // ─── Animation phases ─────────────────────────────────────────────────────
    private static final int PHASE_WAIT    = 0;
    private static final int PHASE_SCATTER = 1;
    private static final int PHASE_PAUSE   = 2;
    private static final int PHASE_GATHER  = 3;
    private static final int PHASE_SHINE   = 4;
    private static final int PHASE_DONE    = 5;

    private static final long DUR_WAIT    = 350;
    private static final long DUR_SCATTER = 700;
    private static final long DUR_PAUSE   = 300;
    private static final long DUR_GATHER  = 1000;
    private static final long DUR_SHINE   = 800;

    private int currentPhase = PHASE_WAIT;
    private float phaseProgress = 0f; // 0..1 within current phase

    // ─── Piece data ───────────────────────────────────────────────────────────
    private static class Piece {
        float[] xs, ys;   // polygon vertices (page-local coords)
        int     color;
        boolean isFold;

        Piece(float[] xs, float[] ys, int color, boolean isFold) {
            this.xs = xs; this.ys = ys; this.color = color; this.isFold = isFold;
        }

        float cx() {
            float s = 0; for (float x : xs) s += x; return s / xs.length;
        }

        float cy() {
            float s = 0; for (float y : ys) s += y; return s / ys.length;
        }
    }

    private List<Piece>  pieces  = new ArrayList<>();
    private float[]      scatterTx, scatterTy, scatterRot;

    // ─── Paints ───────────────────────────────────────────────────────────────
    private final Paint bgPaint      = new Paint();
    private final Paint piecePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pagePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint foldPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint divPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint featurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint logoTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint logoSubPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ─── Star ────────────────────────────────────────────────────────────────
    private float starX, starY;

    // ─── Logo fade ────────────────────────────────────────────────────────────
    private float logoAlpha = 0f;

    // ─── Animators ────────────────────────────────────────────────────────────
    private ValueAnimator waitAnim, scatterAnim, pauseAnim, gatherAnim, shineAnim;

    private final Random rng = new Random();

    // ─── Constructor ─────────────────────────────────────────────────────────
    public SplashView(Context context) {
        super(context);
        float density = context.getResources().getDisplayMetrics().density;
        PW   = 150 * density;
        PH   = 200 * density;
        FOLD = 30  * density;

        setupPaints(density);
    }

    private void setupPaints(float d) {
        bgPaint.setStyle(Paint.Style.FILL);

        pagePaint.setStyle(Paint.Style.FILL);
        pagePaint.setColor(Color.WHITE);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(d);
        borderPaint.setColor(Color.parseColor("#bdbde0"));

        foldPaint.setStyle(Paint.Style.FILL);
        foldPaint.setColor(Color.parseColor("#c8c8ea"));

        divPaint.setStyle(Paint.Style.STROKE);
        divPaint.setStrokeWidth(0.8f * d);
        divPaint.setColor(Color.parseColor("#ddddf0"));

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(13 * d);
        titlePaint.setColor(Color.parseColor("#3949AB"));

        featurePaint.setTextAlign(Paint.Align.CENTER);
        featurePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        featurePaint.setTextSize(11 * d);
        featurePaint.setColor(Color.parseColor("#5C6BC0"));

        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setAntiAlias(true);

        logoTitlePaint.setTextAlign(Paint.Align.CENTER);
        logoTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        logoTitlePaint.setTextSize(22 * d);
        logoTitlePaint.setColor(Color.WHITE);
        logoTitlePaint.setLetterSpacing(0.15f);

        logoSubPaint.setTextAlign(Paint.Align.CENTER);
        logoSubPaint.setTextSize(10 * d);
        logoSubPaint.setColor(Color.parseColor("#80ffffff"));
        logoSubPaint.setLetterSpacing(0.3f);
    }

    // ─── Layout: wait until we have size ─────────────────────────────────────
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (w > 0 && h > 0) {
            buildBackground(w, h);
            starX = w / 2f + PW / 2f + 2;
            starY = h / 2f - PH / 2f + 10;
            initPieces();
            startAnimation();
        }
    }

    private void buildBackground(int w, int h) {
        LinearGradient grad = new LinearGradient(
                0, 0, w, h,
                new int[]{ Color.parseColor("#0f0c29"),
                        Color.parseColor("#1a1245"),
                        Color.parseColor("#0f0c29") },
                new float[]{ 0f, 0.5f, 1f },
                Shader.TileMode.CLAMP
        );
        bgPaint.setShader(grad);
    }

    // ─── Build 10 pieces (5 rows × 2 cols) ───────────────────────────────────
    private void initPieces() {
        pieces.clear();

        // Randomise horizontal cut y-values
        float[] ys = new float[6];
        ys[0] = 0; ys[5] = PH;
        float rowH = PH / 5f;
        for (int i = 1; i < 5; i++) {
            float base = rowH * i;
            float jitter = (rng.nextFloat() - 0.5f) * rowH * 0.45f;
            ys[i] = base + jitter;
        }

        // Vertical cut
        float xCut = PW * (0.4f + rng.nextFloat() * 0.2f);

        int[] shades = {
                Color.parseColor("#fafafa"),
                Color.parseColor("#f3f3fa")
        };

        for (int row = 0; row < 5; row++) {
            float y0 = ys[row], y1 = ys[row + 1];
            for (int col = 0; col < 2; col++) {
                float x0 = col == 0 ? 0 : xCut;
                float x1 = col == 0 ? xCut : PW;
                float[] pxs, pys;

                if (row == 0 && col == 1 && (x1 - FOLD) >= x0) {
                    // top-right piece: fold corner clipped
                    pxs = new float[]{ x0, x1 - FOLD, x1, x1, x0 };
                    pys = new float[]{ y0, y0,         FOLD, y1, y1 };
                } else {
                    pxs = new float[]{ x0, x1, x1, x0 };
                    pys = new float[]{ y0, y0, y1, y1 };
                }

                pieces.add(new Piece(pxs, pys, shades[(row + col) % 2], false));
            }
        }

        // Fold triangle
        pieces.add(new Piece(
                new float[]{ PW - FOLD, PW, PW },
                new float[]{ 0,         0,  FOLD },
                Color.parseColor("#c8c8ea"),
                true
        ));

        buildScatter();
    }

    private void buildScatter() {
        int n = pieces.size();
        scatterTx  = new float[n];
        scatterTy  = new float[n];
        scatterRot = new float[n];

        for (int i = 0; i < n; i++) {
            Piece p = pieces.get(i);
            float dx = p.cx() - PW / 2f;
            float dy = p.cy() - PH / 2f;
            float base = (float) Math.sqrt(dx * dx + dy * dy);
            if (base < 1f) base = 1f;
            float dist   = 180 * getResources().getDisplayMetrics().density
                    + rng.nextFloat() * 140 * getResources().getDisplayMetrics().density;
            float spread = 0.6f + rng.nextFloat() * 0.8f;
            scatterTx[i]  = (dx / base) * dist * spread + (rng.nextFloat() - 0.5f) * 60 * getResources().getDisplayMetrics().density;
            scatterTy[i]  = (dy / base) * dist * spread + (rng.nextFloat() - 0.5f) * 60 * getResources().getDisplayMetrics().density;
            scatterRot[i] = (rng.nextFloat() - 0.5f) * (float) (Math.PI * 1.8);
        }
    }

    // ─── Start animation chain ────────────────────────────────────────────────
    private void startAnimation() {
        currentPhase = PHASE_PAUSE;  // start already scattered
        logoAlpha = 0f;
        phaseProgress = 1f;          // pieces are fully scattered from frame 1
        startPause();
    }
    private void startScatter() {
        scatterAnim = ValueAnimator.ofFloat(0f, 1f);
        scatterAnim.setDuration(DUR_SCATTER);
        scatterAnim.setInterpolator(new AccelerateInterpolator(1.5f));
        scatterAnim.addUpdateListener(a -> {
            phaseProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        scatterAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                currentPhase = PHASE_PAUSE;
                startPause();
            }
        });
        scatterAnim.start();
    }

    private void startPause() {
        pauseAnim = ValueAnimator.ofFloat(0f, 1f);
        pauseAnim.setDuration(DUR_PAUSE);
        pauseAnim.setInterpolator(new LinearInterpolator());
        pauseAnim.addUpdateListener(a -> invalidate());
        pauseAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                currentPhase = PHASE_GATHER;
                startGather();
            }
        });
        pauseAnim.start();
    }

    private void startGather() {
        gatherAnim = ValueAnimator.ofFloat(0f, 1f);
        gatherAnim.setDuration(DUR_GATHER);
        gatherAnim.setInterpolator(new DecelerateInterpolator(1.5f));
        gatherAnim.addUpdateListener(a -> {
            phaseProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        gatherAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                currentPhase = PHASE_SHINE;
                startShine();
            }
        });
        gatherAnim.start();
    }

    private void startShine() {
        shineAnim = ValueAnimator.ofFloat(0f, 1f);
        shineAnim.setDuration(DUR_SHINE);
        shineAnim.setInterpolator(new LinearInterpolator());
        shineAnim.addUpdateListener(a -> {
            phaseProgress = (float) a.getAnimatedValue();
            // Fade in logo text in last 30% of shine
            if (phaseProgress > 0.7f) {
                logoAlpha = (phaseProgress - 0.7f) / 0.3f;
            }
            invalidate();
        });
        shineAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                currentPhase = PHASE_DONE;
                logoAlpha = 1f;
                invalidate();
            }
        });
        shineAnim.start();
    }

    public void stopAnimation() {
        if (waitAnim    != null) waitAnim.cancel();
        if (scatterAnim != null) scatterAnim.cancel();
        if (pauseAnim   != null) pauseAnim.cancel();
        if (gatherAnim  != null) gatherAnim.cancel();
        if (shineAnim   != null) shineAnim.cancel();
    }

    // ─── Draw ─────────────────────────────────────────────────────────────────
    @Override
    protected void onDraw(Canvas canvas) {
        int W = getWidth(), H = getHeight();

        // Background
        canvas.drawRect(0, 0, W, H, bgPaint);

        float ox = W / 2f - PW / 2f;
        float oy = H / 2f - PH / 2f;

        switch (currentPhase) {
            case PHASE_WAIT:
                drawAssembled(canvas, ox, oy, 1f);
                break;

            case PHASE_SCATTER: {
                float e = easeIn(phaseProgress);
                for (int i = 0; i < pieces.size(); i++) {
                    drawPiece(canvas, ox, oy, pieces.get(i),
                            scatterTx[i] * e, scatterTy[i] * e, scatterRot[i] * e, 1f);
                }
                break;
            }

            case PHASE_PAUSE:
                for (int i = 0; i < pieces.size(); i++) {
                    drawPiece(canvas, ox, oy, pieces.get(i),
                            scatterTx[i], scatterTy[i], scatterRot[i], 1f);
                }
                break;

            case PHASE_GATHER: {
                float e = easeInOut(phaseProgress);
                for (int i = 0; i < pieces.size(); i++) {
                    drawPiece(canvas, ox, oy, pieces.get(i),
                            scatterTx[i] * (1 - e), scatterTy[i] * (1 - e),
                            scatterRot[i] * (1 - e), 1f);
                }
                if (phaseProgress > 0.85f) {
                    float a = (phaseProgress - 0.85f) / 0.15f;
                    drawAssembled(canvas, ox, oy, a);
                }
                break;
            }

            case PHASE_SHINE: {
                drawAssembled(canvas, ox, oy, 1f);
                float sp = Math.min(phaseProgress / 0.5f, 1f);
                float eo = sp < 0.65f
                        ? (1f - (float) Math.pow(1 - sp / 0.65f, 3)) * 1.2f
                        : 1.2f - 0.2f * ((sp - 0.65f) / 0.35f);
                float sa = sp < 0.08f ? sp / 0.08f
                        : (phaseProgress > 0.88f ? 1f - (phaseProgress - 0.88f) / 0.12f : 1f);
                drawStar(canvas, starX, starY, Math.max(0f, eo), sa);
                break;
            }

            case PHASE_DONE:
                drawAssembled(canvas, ox, oy, 1f);
                break;
        }

        // Logo text at bottom
        if (logoAlpha > 0f) {
            drawLogoText(canvas, W, H);
        }
    }

    // ─── Draw assembled page ──────────────────────────────────────────────────
    private void drawAssembled(Canvas canvas, float ox, float oy, float alpha) {
        int a = (int)(alpha * 255);
        if (a <= 0) return;

        // Page body path
        Path page = new Path();
        page.moveTo(ox, oy);
        page.lineTo(ox + PW - FOLD, oy);
        page.lineTo(ox + PW, oy + FOLD);
        page.lineTo(ox + PW, oy + PH);
        page.lineTo(ox, oy + PH);
        page.close();

        pagePaint.setAlpha(a);
        canvas.drawPath(page, pagePaint);

        borderPaint.setAlpha(a);
        canvas.drawPath(page, borderPaint);

        // Fold triangle
        Path fold = new Path();
        fold.moveTo(ox + PW - FOLD, oy);
        fold.lineTo(ox + PW, oy);
        fold.lineTo(ox + PW, oy + FOLD);
        fold.close();
        foldPaint.setAlpha(a);
        canvas.drawPath(fold, foldPaint);

        // Fold crease lines
        Paint crease = new Paint(Paint.ANTI_ALIAS_FLAG);
        crease.setStyle(Paint.Style.STROKE);
        crease.setStrokeWidth(0.8f * getResources().getDisplayMetrics().density);
        crease.setColor(Color.parseColor("#aaaacc"));
        crease.setAlpha(a);
        canvas.drawLine(ox + PW - FOLD, oy, ox + PW - FOLD, oy + FOLD, crease);
        canvas.drawLine(ox + PW - FOLD, oy + FOLD, ox + PW, oy + FOLD, crease);

        float midX = ox + PW / 2f;

        // App name
        titlePaint.setAlpha(a);
        canvas.drawText("PDF Studio", midX, oy + 30 * getResources().getDisplayMetrics().density, titlePaint);

        // Divider
        divPaint.setAlpha(a);
        canvas.drawLine(
                ox + 20 * getResources().getDisplayMetrics().density, oy + 40 * getResources().getDisplayMetrics().density,
                ox + PW - 20 * getResources().getDisplayMetrics().density, oy + 40 * getResources().getDisplayMetrics().density,
                divPaint
        );

        float d = getResources().getDisplayMetrics().density;

        // Feature 1
        featurePaint.setAlpha(a);
        canvas.drawText("\u270F  Edit PDF", midX, oy + 100 * d, featurePaint);

        // Subtle line between features
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(0.7f * d);
        linePaint.setColor(Color.parseColor("#e8e8f5"));
        linePaint.setAlpha(a);
        canvas.drawLine(ox + 35 * d, oy + 107 * d, ox + PW - 35 * d, oy + 107 * d, linePaint);

        // Feature 2
        canvas.drawText("\u26A1  Compress PDF", midX, oy + 126 * d, featurePaint);
    }

    // ─── Draw single scattered piece ─────────────────────────────────────────
    private void drawPiece(Canvas canvas, float ox, float oy, Piece piece,
                           float dx, float dy, float rot, float alpha) {
        int a = (int)(alpha * 255);
        if (a <= 0 || piece.xs.length < 3) return;

        float cx = piece.cx(), cy = piece.cy();

        canvas.save();
        canvas.translate(ox + cx + dx, oy + cy + dy);
        canvas.rotate((float) Math.toDegrees(rot));
        canvas.translate(-cx, -cy);

        Path path = new Path();
        path.moveTo(piece.xs[0], piece.ys[0]);
        for (int i = 1; i < piece.xs.length; i++) {
            path.lineTo(piece.xs[i], piece.ys[i]);
        }
        path.close();

        piecePaint.setStyle(Paint.Style.FILL);
        piecePaint.setColor(piece.color);
        piecePaint.setAlpha(a);
        canvas.drawPath(path, piecePaint);

        canvas.restore();
    }

    // ─── Draw shining star ────────────────────────────────────────────────────
    private void drawStar(Canvas canvas, float x, float y, float scale, float alpha) {
        if (alpha <= 0f || scale <= 0f) return;
        int a = (int)(alpha * 255);

        // Glow
        RadialGradient glow = new RadialGradient(
                x, y, 60 * scale,
                new int[]{ 0xB3FFF050, 0x33FFC832, 0x00FFC832 },
                new float[]{ 0f, 0.5f, 1f },
                Shader.TileMode.CLAMP
        );
        starPaint.setShader(glow);
        starPaint.setAlpha(a);
        canvas.drawCircle(x, y, 36 * scale, starPaint);
        starPaint.setShader(null);

        // Star polygon (5-pointed)
        int spikes = 5;
        float outer = 28 * scale;  // was 15
        float inner = 11 * scale;  // was 6 — keep roughly 40% of outer
        Path star = new Path();
        for (int i = 0; i < spikes * 2; i++) {
            float r   = (i % 2 == 0) ? outer : inner;
            float ang = (float)(i * Math.PI / spikes - Math.PI / 2);
            float px  = x + (float) Math.cos(ang) * r;
            float py  = y + (float) Math.sin(ang) * r;
            if (i == 0) star.moveTo(px, py); else star.lineTo(px, py);
        }
        star.close();

        RadialGradient starGrad = new RadialGradient(
                x, y - outer * 0.3f, outer,
                new int[]{ 0xFFFFfbe0, 0xFFFFD700, 0xFFFFAA00 },
                new float[]{ 0f, 0.5f, 1f },
                Shader.TileMode.CLAMP
        );
        starPaint.setShader(starGrad);
        starPaint.setAlpha(a);
        canvas.drawPath(star, starPaint);
        starPaint.setShader(null);

        // Sparkle lines
        Paint sparkle = new Paint(Paint.ANTI_ALIAS_FLAG);
        sparkle.setStyle(Paint.Style.STROKE);
        sparkle.setStrokeWidth(1.4f * scale);
        sparkle.setColor(Color.parseColor("#FFFFF064"));
        sparkle.setAlpha(a);
        sparkle.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i < 4; i++) {
            float ang = (float)(i * Math.PI / 2 + Math.PI / 8);
            float cos = (float) Math.cos(ang);
            float sin = (float) Math.sin(ang);
            canvas.drawLine(
                    x + cos * (outer + 1), y + sin * (outer + 1),
                    x + cos * (outer + 10 * scale), y + sin * (outer + 10 * scale),
                    sparkle
            );
        }
    }

    // ─── Draw bottom logo text ────────────────────────────────────────────────
    private void drawLogoText(Canvas canvas, int W, int H) {
        int a = (int)(logoAlpha * 255);
        float d = getResources().getDisplayMetrics().density;

        logoTitlePaint.setAlpha(a);
        canvas.drawText("PDF STUDIO", W / 2f, H - 55 * d, logoTitlePaint);

        logoSubPaint.setAlpha(a);
        canvas.drawText("YOUR PDF TOOLKIT", W / 2f, H - 38 * d, logoSubPaint);
    }

    // ─── Easing ───────────────────────────────────────────────────────────────
    private float easeIn(float t) {
        return t * t * t;
    }

    private float easeInOut(float t) {
        return t < 0.5f
                ? 4 * t * t * t
                : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
}