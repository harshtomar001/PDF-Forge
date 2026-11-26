package com.harsh.pdfstudio.storage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.harsh.pdfstudio.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class StorageDonutView extends View {

    /** A single arc segment of the donut, expressed as a percentage (0-100) of the ring. */
    public static class Segment {
        public final float percentage;
        public final int color;

        public Segment(float percentage, int color) {
            this.percentage = percentage;
            this.color = color;
        }
    }

    private List<Segment> segments = Collections.emptyList();

    private float strokeWidthPx;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    /** Gap between segments, in degrees, purely for visual separation. */
    private static final float SEGMENT_GAP_DEGREES = 3f;

    /** Start at 12 o'clock. */
    private static final float START_ANGLE = -90f;

    public StorageDonutView(Context context) {
        this(context, null);
    }

    public StorageDonutView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StorageDonutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        strokeWidthPx = getResources().getDimension(R.dimen.storage_donut_stroke_width);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.storage_track_unused));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setSegments(List<Segment> newSegments) {

        this.segments = newSegments != null ? newSegments : new ArrayList<Segment>();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        trackPaint.setStrokeWidth(strokeWidthPx);
        segmentPaint.setStrokeWidth(strokeWidthPx);

        float inset = strokeWidthPx / 2f;
        arcRect.set(inset, inset, w - inset, h - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Background track (the "unused" portion is implied by drawing a full circle first).
        canvas.drawOval(arcRect, trackPaint);

        if (segments.isEmpty()) return;

        float totalGap = SEGMENT_GAP_DEGREES * segments.size();
        float availableDegrees = 360f - totalGap;
        float currentAngle = START_ANGLE;

        for (Segment segment : segments) {
            float sweep = (segment.percentage / 100f) * availableDegrees;
            if (sweep <= 0f) continue;
            segmentPaint.setColor(segment.color);
            canvas.drawArc(arcRect, currentAngle, sweep, false, segmentPaint);
            currentAngle += sweep + SEGMENT_GAP_DEGREES;
        }
    }
}
