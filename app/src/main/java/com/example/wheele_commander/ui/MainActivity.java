package com.example.wheele_commander.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.wheele_commander.R;
import com.example.wheele_commander.backend.NetworkClient;
import com.example.wheele_commander.viewmodel.BatteryViewModel;
import com.example.wheele_commander.viewmodel.JoystickViewModel;
import com.example.wheele_commander.viewmodel.MovementStatisticsViewModel;
import com.example.wheele_commander.viewmodel.WarningViewModel;

import io.github.controlwear.virtual.joystick.android.JoystickView;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

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
                joystickViewModel.onJoystickMove(angle, strength), 17);

        // observe view model variables and change views accordingly
        batteryViewModel.getBatteryCharge().observe(this, batteryLevel ->
                batteryView.setBatteryLevel(batteryLevel / 100f));
        batteryViewModel.getEstimatedMileage().observe(this, estimatedMileage ->
                mileageTextView.setText(String.format("%d km", estimatedMileage)));
        movementViewModel.getVelocity().observe(this, speedometerView::setVelocity);
        movementViewModel.getDistanceTravelled().observe(this, distanceTravelled ->
                traveledTextView.setText(String.format("%d km", distanceTravelled)));

        // bind view models to the service
        Intent startIntent = new Intent(this, NetworkClient.class);
        startService(startIntent);

        Intent bindIntent = new Intent(this, NetworkClient.class);
        bindService(bindIntent, joystickViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
        bindService(bindIntent, batteryViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
        bindService(bindIntent, movementViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
        bindService(bindIntent, warningViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }
}