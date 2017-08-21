package com.fullwipe.fingerprintdrive.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.Shape;

/**
 * Created by Valentina Lipari, Meo Giovanni and Illiano Francesca on 17/08/17.
 */

public final class FoldCornerCard extends Shape {

    private final float foldPart;
    private final Path cardPath = new Path();
    private final Path foldPath = new Path();
    private final Paint foldPaint;

    public FoldCornerCard(int foldColor, float foldPart) {
        if (foldPart <= 0 || foldPart >= 1) {
            throw new IllegalArgumentException("Fold part must be in (0,1)");
        }
        this.foldPart = foldPart;
        this.foldPaint = new Paint();
        foldPaint.setAntiAlias(true);
        foldPaint.setColor(foldColor);
    }

    @Override
    protected void onResize(float width, float height) {
        super.onResize(width, height);
        this.cardPath.reset();
        final float leftFold = width - width * foldPart;
        final float bottomFold = height * foldPart;

        cardPath.lineTo(leftFold, 0);
        cardPath.lineTo(width, bottomFold);
        cardPath.lineTo(width, height);
        cardPath.lineTo(0, height);
        cardPath.close();

        foldPath.reset();
        foldPath.moveTo(leftFold, 0);
        foldPath.lineTo(leftFold, bottomFold);
        foldPath.lineTo(width, bottomFold);
        foldPath.close();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(cardPath, paint);
        canvas.drawPath(foldPath, foldPaint);
    }
}
