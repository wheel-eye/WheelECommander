package com.example.wheele_commander.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.wheele_commander.R;
import com.example.wheele_commander.viewmodel.BatteryViewModel;
import com.example.wheele_commander.viewmodel.JoystickViewModel;
import com.example.wheele_commander.viewmodel.MovementStatisticsViewModel;
import com.example.wheele_commander.viewmodel.WarningViewModel;

import java.util.concurrent.atomic.AtomicReference;

import io.github.controlwear.virtual.joystick.android.JoystickView;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {
    private ViewModelProvider viewModelProvider;
    private MovementStatisticsViewModel movementViewModel;
    private BatteryViewModel batteryViewModel;
    private WarningViewModel warningViewModel;
    private JoystickViewModel joystickViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModelProvider = new ViewModelProvider(this);

        joystickViewModel = viewModelProvider.get(JoystickViewModel.class);
        JoystickView joystickView = findViewById(R.id.joystickView);
        joystickView.setOnMoveListener((angle, strength) ->
                joystickViewModel.onJoystickMove(angle, strength));

        BatteryView batteryView = findViewById(R.id.batteryView);
        batteryViewModel = viewModelProvider.get(BatteryViewModel.class);
        batteryViewModel.getBatteryCharge().observe(this, batteryLevel ->
                batteryView.setBatteryLevel(batteryLevel / 100f));

        SpeedometerView speedometerView = findViewById(R.id.speedometerView);
        movementViewModel = viewModelProvider.get(MovementStatisticsViewModel.class);
        movementViewModel.getVelocity().observe(this, velocity -> {
            speedometerView.setVelocity(velocity * 3.6f);
        });

        // only for testing purposes
        AtomicReference<Float> s = new AtomicReference<>(0f);
        AtomicReference<Float> b = new AtomicReference<>(0f);
        Button buttonPlus = findViewById(R.id.plus);
        Button buttonMinus = findViewById(R.id.minus);
        buttonPlus.setOnClickListener(view -> {
            s.updateAndGet(v -> v + 0.25f);
            b.updateAndGet(v -> v + 0.05f);
            System.out.println(b.get());
            speedometerView.setVelocity(s.get());
            batteryView.setBatteryLevel(b.get());
        });
        buttonMinus.setOnClickListener(view -> {
            s.updateAndGet(v -> v - 0.25f);
            b.updateAndGet(v -> v - 0.05f);
            speedometerView.setVelocity(s.get());
            batteryView.setBatteryLevel(b.get());
        });
    }
}