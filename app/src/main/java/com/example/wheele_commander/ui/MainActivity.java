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
    private JoystickViewModel joystickViewModel;
    private BatteryViewModel batteryViewModel;
    private MovementStatisticsViewModel movementViewModel;
    private WarningViewModel warningViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiate views
        JoystickView joystickView = findViewById(R.id.joystickView);
        BatteryView batteryView = findViewById(R.id.batteryView);
        TextView mileageTextView = findViewById(R.id.mileageTextView);
        SpeedometerView speedometerView = findViewById(R.id.speedometerView);
        TextView traveledTextView = findViewById(R.id.traveledTextView);

        // instantiate view models
        viewModelProvider = new ViewModelProvider(this);
        joystickViewModel = viewModelProvider.get(JoystickViewModel.class);
        batteryViewModel = viewModelProvider.get(BatteryViewModel.class);
        movementViewModel = viewModelProvider.get(MovementStatisticsViewModel.class);
        warningViewModel = viewModelProvider.get(WarningViewModel.class);

        joystickView.setOnMoveListener((angle, strength) ->
                joystickViewModel.onJoystickMove(angle, strength));

        // observe view model variables and change views accordingly
        batteryViewModel.getBatteryCharge().observe(this, batteryLevel ->
                batteryView.setBatteryLevel(batteryLevel / 100f));
        batteryViewModel.getEstimatedMileage().observe(this, estimatedMileage ->
                mileageTextView.setText(String.format("%d km", estimatedMileage)));
        movementViewModel.getVelocity().observe(this, velocity ->
                speedometerView.setVelocity(velocity * 3.6f));
        movementViewModel.getDistanceTravelled().observe(this, distanceTravelled ->
                traveledTextView.setText(String.format("%d km", distanceTravelled)));

        // bind view models to the service
        Intent intent = new Intent(this, NetworkClient.class);
        bindService(intent, joystickViewModel.getConnection(), Context.BIND_AUTO_CREATE);
        bindService(intent, batteryViewModel.getConnection(), Context.BIND_AUTO_CREATE);
        bindService(intent, movementViewModel.getConnection(), Context.BIND_AUTO_CREATE);
        bindService(intent, warningViewModel.getConnection(), Context.BIND_AUTO_CREATE);

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