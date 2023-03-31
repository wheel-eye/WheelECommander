package com.example.wheele_commander.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.wheele_commander.R;

public class BatteryView extends View {
    private static final float DEFAULT_AMPLITUDE_RATIO = 0.05f;
    private static final float DEFAULT_BATTERY_LEVEL_RATIO = 0.5f;
    private static final float DEFAULT_WAVE_LENGTH_RATIO = 1.0f;
    private static final float DEFAULT_WAVE_SHIFT_RATIO = 0.0f;

    public static final int DEFAULT_BEHIND_WAVE_COLOR = Color.parseColor("#28FFFFFF");
    public static final int DEFAULT_FRONT_WAVE_COLOR = Color.parseColor("#3CFFFFFF");
    public static final int DEFAULT_BORDER_COLOR = Color.parseColor("#21304F");
    public static final int DEFAULT_BORDER_WIDTH = 30;

    private BitmapShader waveShader;
    private Matrix shaderMatrix;
    private Paint viewPaint;
    private Paint borderPaint;
    private Paint textPaint;

    private float defaultAmplitude;
    private float defaultWaterLevel;
    private float defaultWaveLength;
    private double defaultAngularFrequency;

    private float amplitudeRatio = DEFAULT_AMPLITUDE_RATIO;
    private float waveLengthRatio = DEFAULT_WAVE_LENGTH_RATIO;
    private float batteryLevel = DEFAULT_BATTERY_LEVEL_RATIO;
    private float waveShiftRatio = DEFAULT_WAVE_SHIFT_RATIO;

    private int behindWaveColor = DEFAULT_BEHIND_WAVE_COLOR;
    private int frontWaveColor = DEFAULT_FRONT_WAVE_COLOR;
    private int borderColor = DEFAULT_BORDER_COLOR;
    private int borderWidth = DEFAULT_BORDER_WIDTH;
    private final Rect textBounds = new Rect();

    public BatteryView(Context context) {
        super(context);
        init();
        startAnimation();
    }

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        startAnimation();
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
        startAnimation();
    }

    public void startAnimation() {
        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
                this, "waveShiftRatio", 0f, 1f);
        waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnim.setDuration(1000L);
        waveShiftAnim.setInterpolator(new LinearInterpolator());
        waveShiftAnim.start();
    }

    private void init() {
        shaderMatrix = new Matrix();

        viewPaint = new Paint();
        viewPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(135f);
    }

    private void init(AttributeSet attrs) {
        init();

        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.WaveView, 0, 0);

        amplitudeRatio = typedArray.getFloat(R.styleable.WaveView_waveAmplitudeRatio, DEFAULT_AMPLITUDE_RATIO);
        batteryLevel = typedArray.getFloat(R.styleable.WaveView_waveWaterLevel, DEFAULT_BATTERY_LEVEL_RATIO);
        waveLengthRatio = typedArray.getFloat(R.styleable.WaveView_waveLengthRatio, DEFAULT_WAVE_LENGTH_RATIO);
        waveShiftRatio = typedArray.getFloat(R.styleable.WaveView_waveShiftRatio, DEFAULT_WAVE_SHIFT_RATIO);
        frontWaveColor = typedArray.getColor(R.styleable.WaveView_waveFrontColor, DEFAULT_FRONT_WAVE_COLOR);
        behindWaveColor = typedArray.getColor(R.styleable.WaveView_waveBackColor, DEFAULT_BEHIND_WAVE_COLOR);
        borderColor = typedArray.getColor(R.styleable.WaveView_waveBorderColor, DEFAULT_BORDER_COLOR);
        borderWidth = typedArray.getInt(R.styleable.WaveView_waveBorderWidth, DEFAULT_BORDER_WIDTH);

        typedArray.recycle();

        setBorder(borderWidth, borderColor);
    }

    public float getWaveShiftRatio() {
        return waveShiftRatio;
    }

    public void setWaveShiftRatio(float waveShiftRatio) {
        if (this.waveShiftRatio == waveShiftRatio)
            return;

        this.waveShiftRatio = waveShiftRatio;
        invalidate();
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(float newBatteryLevel) {
        // TODO: floating point precision loss when approaching 1
        if (batteryLevel == newBatteryLevel || newBatteryLevel < 0f || newBatteryLevel > 1f)
            return;

        ValueAnimator animator = ValueAnimator.ofFloat(batteryLevel, newBatteryLevel);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            batteryLevel = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.setDuration(200L);
        animator.start();
    }

    public float getAmplitudeRatio() {
        return amplitudeRatio;
    }

    public void setAmplitudeRatio(float amplitudeRatio) {
        if (this.amplitudeRatio != amplitudeRatio) {
            this.amplitudeRatio = amplitudeRatio;
            invalidate();
        }
    }

    public float getWaveLengthRatio() {
        return waveLengthRatio;
    }

    public void setWaveLengthRatio(float waveLengthRatio) {
        this.waveLengthRatio = waveLengthRatio;
    }

    public void setBorder(int width, int color) {
        if (borderPaint == null) {
            borderPaint = new Paint();
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Style.STROKE);
        }
        borderPaint.setColor(color);
        borderPaint.setStrokeWidth(width);

        invalidate();
    }

    public void setWaveColor(int behindWaveColor, int frontWaveColor) {
        this.behindWaveColor = behindWaveColor;
        this.frontWaveColor = frontWaveColor;

        if (getWidth() > 0 && getHeight() > 0) {
            // need to recreate shader when color changed
            waveShader = null;
            createShader();
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);

        createShader();
    }

    private void createShader() {
        defaultAngularFrequency = 2.0f * Math.PI / DEFAULT_WAVE_LENGTH_RATIO / getWidth();
        defaultAmplitude = getHeight() * DEFAULT_AMPLITUDE_RATIO;
        defaultWaterLevel = getHeight() * DEFAULT_BATTERY_LEVEL_RATIO;
        defaultWaveLength = getWidth();

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint wavePaint = new Paint();
        wavePaint.setStrokeWidth(2);
        wavePaint.setAntiAlias(true);

        // Draw default waves into the bitmap
        // y=Asin(ωx+φ)+h
        final int endX = getWidth() + 1;
        final int endY = getHeight() + 1;

        float[] waveY = new float[endX];

        wavePaint.setColor(behindWaveColor);
        for (int beginX = 0; beginX < endX; beginX++) {
            double wx = beginX * defaultAngularFrequency;
            float beginY = (float) (defaultWaterLevel + defaultAmplitude * Math.sin(wx));
            canvas.drawLine(beginX, beginY, beginX, endY, wavePaint);

            waveY[beginX] = beginY;
        }

        wavePaint.setColor(frontWaveColor);
        final int wave2Shift = (int) (defaultWaveLength / 4);
        for (int beginX = 0; beginX < endX; beginX++) {
            canvas.drawLine(beginX, waveY[(beginX + wave2Shift) % endX], beginX, endY, wavePaint);
        }

        // use the bitamp to create the shader
        waveShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        viewPaint.setShader(waveShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (waveShader != null) {
            if (viewPaint.getShader() == null)
                viewPaint.setShader(waveShader);

            shaderMatrix.setScale(
                    waveLengthRatio / DEFAULT_WAVE_LENGTH_RATIO,
                    amplitudeRatio / DEFAULT_AMPLITUDE_RATIO,
                    0,
                    defaultWaterLevel);
            shaderMatrix.postTranslate(
                    waveShiftRatio * getWidth(),
                    (DEFAULT_BATTERY_LEVEL_RATIO - batteryLevel) * getHeight());

            waveShader.setLocalMatrix(shaderMatrix);

            float borderWidth = borderPaint == null ? 0f : borderPaint.getStrokeWidth();
            if (borderWidth > 0)
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (getWidth() - borderWidth) / 2f - 1f, borderPaint);

            float radius = getWidth() / 2f - borderWidth;
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, viewPaint);
        } else
            viewPaint.setShader(null);

        String levelPercent = (int) (batteryLevel * 100) + "%";
        textPaint.getTextBounds(levelPercent, 0, levelPercent.length(), textBounds);
        canvas.drawText(levelPercent, getWidth() / 2f - textBounds.width() / 2f, getHeight() / 2f + textBounds.height() / 2f, textPaint);
    }
}