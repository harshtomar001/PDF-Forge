package com.harsh.pdfstudio.model;

import android.graphics.PointF;
import android.graphics.RectF;

public class RotatedBounds {

    public PointF topLeft;
    public PointF topRight;
    public PointF bottomLeft;
    public PointF bottomRight;

    public float minX;
    public float maxX;

    public float minY;
    public float maxY;

    public RectF boundingRect;
    public PointF deletePoint;
}