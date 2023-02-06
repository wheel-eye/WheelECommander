package com.example.wheele_commander.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.wheele_commander.R;

import java.util.concurrent.atomic.AtomicReference;

import io.github.controlwear.virtual.joystick.android.JoystickView;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JoystickView joystickView = findViewById(R.id.joystickView);
        joystickView.setOnMoveListener((angle, strength) -> {
            System.out.println(angle + " " + strength);
        });

        ConstraintLayout layout = findViewById(R.id.constraintLayout);
        layout.setOnTouchListener((view, motionEvent) -> {
            int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                joystickView.setX(motionEvent.getX() - joystickView.getWidth() / 2f);
                joystickView.setY(motionEvent.getY() - joystickView.getHeight() / 2f);
            }

            return false;
        });

        SpeedometerView speedometerView = findViewById(R.id.speedometerView);

        BatteryView batteryView = findViewById(R.id.batteryView);
        batteryView.setBatteryLevel(0f);
        batteryView.setBorder(30, Color.parseColor("#21304F"));
        batteryView.start();

        AtomicReference<Float> s = new AtomicReference<>(0f);
        AtomicReference<Float> b = new AtomicReference<>(0f);
        Button buttonPlus = findViewById(R.id.plus);
        Button buttonMinus = findViewById(R.id.minus);
        buttonPlus.setOnClickListener(view -> {
            s.updateAndGet(v -> v + 0.25f);
            b.updateAndGet(v -> v + 0.05f);
            speedometerView.setSpeed(s.get(), 400L);
            batteryView.setBatteryLevel(b.get());
        });
        buttonMinus.setOnClickListener(view -> {
            s.updateAndGet(v -> v - 0.25f);
            b.updateAndGet(v -> v - 0.05f);
            speedometerView.setSpeed(s.get(), 400L);
            batteryView.setBatteryLevel(b.get());
        });
    }
}