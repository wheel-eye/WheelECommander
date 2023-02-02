package com.example.wheele_commander.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import com.example.wheele_commander.R;

public class SpeedometerView extends View {
    public static final float MIN_ANGLE = 220f;
    public static final float MAX_ANGLE = -40f;
    public static final float START_ANGLE = 140f;
    public static final float SWEEP_ANGLE = 260f;
    public static final int MIN_SPEED = 0;
    public static final int MINOR_TICK_STEP = 1;
    public static final int MAJOR_TICK_STEP = 2;
    public static final float TICK_MARGIN = 10f;
    public static final float TICK_TEXT_MARGIN = 30f;
    public static final float MAJOR_TICK_SIZE = 50f;
    public static final float MINOR_TICK_SIZE = 30f;
    public static final float MAJOR_TICK_WIDTH = 8f;
    public static final float MINOR_TICK_WIDTH = 4f;

    private int maxSpeed = 10;
    private float borderSize = 36f;
    private float textGap = 50f;
    private int borderColor = Color.parseColor("#402c47");
    private int fillColor = Color.parseColor("#d83a78");
    private int textColor = Color.parseColor("#f5f5f5");
    private String metricText = "km/h";
    private final RectF indicatorBorderRect = new RectF();
    private final RectF tickBorderRect = new RectF();
    private final Rect textBounds = new Rect();
    private float angle = MIN_ANGLE;
    private int speed = 0;
    private float centerX = getWidth() / 2f;
    private float centerY = getWidth() / 2f;

    // Dimension Getters
    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    // Core Attributes
    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int value) {
        maxSpeed = value;
        invalidate();
    }

    public float getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(float value) {
        borderSize = value;
        paintIndicatorBorder.setStrokeWidth(value);
        paintIndicatorFill.setStrokeWidth(value);
        invalidate();
    }

    public float getTextGap() {
        return textGap;
    }

    public void setTextGap(float value) {
        textGap = value;
        invalidate();
    }

    public String getMetricText() {
        return metricText;
    }

    public void setMetricText(String value) {
        metricText = value;
        invalidate();
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int value) {
        borderColor = value;
        paintIndicatorBorder.setColor(value);
        paintTickBorder.setColor(value);
        paintMajorTick.setColor(value);
        paintMinorTick.setColor(value);
        invalidate();
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int value) {
        fillColor = value;
        paintIndicatorFill.setColor(value);
        invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int value) {
        textColor = value;
        paintTickText.setColor(value);
        paintSpeed.setColor(value);
        paintMetric.setColor(value);
        invalidate();
    }

    public SpeedometerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        obtainStyledAttributes(attrs, 0);
    }

    public SpeedometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttributes(attrs, defStyleAttr);
    }

    private void obtainStyledAttributes(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.Speedometer, defStyleAttr, 0);
        try {
            setMaxSpeed(typedArray.getInt(R.styleable.Speedometer_maxSpeed, maxSpeed));
            setBorderSize(typedArray.getDimension(R.styleable.Speedometer_borderSize, borderSize));
            setTextGap(typedArray.getDimension(R.styleable.Speedometer_textGap, textGap));
            setMetricText(typedArray.getString(R.styleable.Speedometer_metricText));
            setBorderColor(typedArray.getColor(R.styleable.Speedometer_borderColor, borderColor));
            setFillColor(typedArray.getColor(R.styleable.Speedometer_fillColor, borderColor));
            setTextColor(typedArray.getColor(R.styleable.Speedometer_textColor, borderColor));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        renderMajorTicks(canvas);
        renderMinorTicks(canvas);
        renderBorder(canvas);
        renderBorderFill(canvas);
        renderTickBorder(canvas);
        renderSpeedAndMetricText(canvas);
    }

    private final Paint paintIndicatorBorder = new Paint();

    {
        paintIndicatorBorder.setAntiAlias(true);
        paintIndicatorBorder.setStyle(Paint.Style.STROKE);
        paintIndicatorBorder.setColor(borderColor);
        paintIndicatorBorder.setStrokeWidth(borderSize);
        paintIndicatorBorder.setStrokeCap(Paint.Cap.ROUND);
    }

    private final Paint paintIndicatorFill = new Paint();

    {
        paintIndicatorFill.setAntiAlias(true);
        paintIndicatorFill.setStyle(Paint.Style.STROKE);
        paintIndicatorFill.setColor(fillColor);
        paintIndicatorFill.setStrokeWidth(borderSize);
        paintIndicatorFill.setStrokeCap(Paint.Cap.ROUND);
    }

    private final Paint paintTickBorder = new Paint();

    {
        paintTickBorder.setAntiAlias(true);
        paintTickBorder.setStyle(Paint.Style.STROKE);
        paintTickBorder.setColor(borderColor);
        paintTickBorder.setStrokeWidth(4f);
        paintTickBorder.setStrokeCap(Paint.Cap.ROUND);
    }

    private final Paint paintMajorTick = new Paint();

    {
        paintMajorTick.setAntiAlias(true);
        paintMajorTick.setStyle(Paint.Style.STROKE);
        paintMajorTick.setColor(borderColor);
        paintMajorTick.setStrokeWidth(MAJOR_TICK_WIDTH);
        paintMajorTick.setStrokeCap(Paint.Cap.BUTT);
    }

    private final Paint paintMinorTick = new Paint();

    {
        paintMinorTick.setAntiAlias(true);
        paintMinorTick.setStyle(Paint.Style.STROKE);
        paintMinorTick.setColor(borderColor);
        paintMinorTick.setStrokeWidth(MINOR_TICK_WIDTH);
        paintMinorTick.setStrokeCap(Paint.Cap.BUTT);
    }

    private final Paint paintTickText = new Paint();

    {
        paintTickText.setAntiAlias(true);
        paintTickText.setStyle(Paint.Style.FILL);
        paintTickText.setColor(textColor);
        paintTickText.setTextSize(50f);
    }

    private final Paint paintSpeed = new Paint();

    {
        paintSpeed.setAntiAlias(true);
        paintSpeed.setStyle(Paint.Style.FILL);
        paintSpeed.setColor(textColor);
        paintSpeed.setTextSize(260f);
    }

    private final Paint paintMetric = new Paint();

    {
        paintMetric.setAntiAlias(true);
        paintMetric.setStyle(Paint.Style.FILL);
        paintMetric.setColor(textColor);
        paintMetric.setTextSize(50f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int width, int height, int widthOld, int heightOld) {
        super.onSizeChanged(width, height, widthOld, heightOld);
        indicatorBorderRect.set(
                borderSize / 2,
                borderSize / 2,
                getWidth() - borderSize / 2,
                getWidth() - borderSize / 2);
        tickBorderRect.set(
                borderSize + TICK_MARGIN,
                borderSize + TICK_MARGIN,
                getWidth() - borderSize - TICK_MARGIN,
                getWidth() - borderSize - TICK_MARGIN);
    }

    private void renderMinorTicks(Canvas canvas) {
        // TODO: Replace properly!
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;
        for (int speed = MIN_SPEED; speed <= maxSpeed; speed += MINOR_TICK_STEP) {
            if (speed % MAJOR_TICK_STEP != 0) {
                double angle = Math.toRadians(mapSpeedToAngle(speed));
                float cosAngle = (float) Math.cos(angle);
                float sinAngle = (float) Math.sin(angle);
                canvas.drawLine(
                        (centerX + (centerX - borderSize - MINOR_TICK_SIZE)) * cosAngle,
                        (centerY - (centerY - borderSize - MINOR_TICK_SIZE)) * sinAngle,
                        (centerX + (centerX - borderSize - TICK_MARGIN)) * cosAngle,
                        (centerY - (centerY - borderSize - TICK_MARGIN)) * sinAngle,
                        paintMinorTick);
            }
        }
    }

    private void renderMajorTicks(Canvas canvas) {
        // TODO: Replace properly!
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;
        for (int speed = MIN_SPEED; speed <= maxSpeed; speed += MAJOR_TICK_STEP) {
            double angle = Math.toRadians(mapSpeedToAngle(speed));
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            canvas.drawLine(
                    centerX + (centerX - borderSize - MAJOR_TICK_SIZE) * cos,
                    centerY - (centerY - borderSize - MAJOR_TICK_SIZE) * sin,
                    centerX + (centerX - borderSize - TICK_MARGIN) * cos,
                    centerY - (centerY - borderSize - TICK_MARGIN) * sin,
                    paintMajorTick);
            drawTextCentred(canvas, Integer.toString(speed),
                    centerX + (centerX - borderSize - MAJOR_TICK_SIZE - TICK_MARGIN - TICK_TEXT_MARGIN) * cos,
                    centerY - (centerY - borderSize - MAJOR_TICK_SIZE - TICK_MARGIN - TICK_TEXT_MARGIN) * sin,
                    paintTickText);
        }
    }

    private void renderBorder(Canvas canvas) {
        canvas.drawArc(indicatorBorderRect, 140f, 260f, false, paintIndicatorBorder);
    }

    private void renderTickBorder(Canvas canvas) {
        canvas.drawArc(tickBorderRect, START_ANGLE, SWEEP_ANGLE, false, paintTickBorder);
    }

    private void renderBorderFill(Canvas canvas) {
        canvas.drawArc(indicatorBorderRect, START_ANGLE, MIN_ANGLE - angle, false, paintIndicatorFill);
    }

    private void renderSpeedAndMetricText(Canvas canvas) {
        drawTextCentred(canvas, Integer.toString(speed), getWidth() / 2f, getHeight() / 2f, paintSpeed);
        drawTextCentred(canvas, metricText, getWidth() / 2f, getHeight() / 2f + paintSpeed.getTextSize() / 2f + textGap, paintMetric);
    }

    private float mapSpeedToAngle(int speed) {
        return (MIN_ANGLE + ((MAX_ANGLE - MIN_ANGLE) / (maxSpeed - MIN_SPEED)) * (speed - MIN_SPEED));
    }

    private int mapAngleToSpeed(float angle) {
        return (int) (MIN_SPEED + ((maxSpeed - MIN_SPEED) / (MAX_ANGLE - MIN_ANGLE)) * (angle - MIN_ANGLE));
    }

    public void setSpeed(int speedToSet, long durationMillis) {
        ValueAnimator animator = ValueAnimator.ofFloat(mapSpeedToAngle(speed), mapSpeedToAngle(speedToSet));
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            angle = (float) valueAnimator.getAnimatedValue();
            speed = mapAngleToSpeed(angle);
            invalidate();
        });
        animator.setDuration(durationMillis);
        animator.start();
    }

    private void drawTextCentred(Canvas canvas, String text, double cx, double cy, Paint paint) {
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, (float) (cx - textBounds.exactCenterX()), (float) (cy - textBounds.exactCenterY()), paint);
    }
}
