package com.example.wheele_commander.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.wheele_commander.R;
import com.example.wheele_commander.backend.bluetooth.BluetoothService;
import com.example.wheele_commander.viewmodel.AbstractViewModel;
import com.example.wheele_commander.viewmodel.BatteryViewModel;
import com.example.wheele_commander.viewmodel.JoystickViewModel;
import com.example.wheele_commander.viewmodel.MovementStatisticsViewModel;
import com.example.wheele_commander.viewmodel.WarningViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        JoystickView joystickView = findViewById(R.id.joystickView);
        BatteryView batteryView = findViewById(R.id.batteryView);
        TextView mileageTextView = findViewById(R.id.mileageTextView);
        SpeedometerView speedometerView = findViewById(R.id.speedometerView);
        TextView traveledTextView = findViewById(R.id.traveledTextView);

        constraintLayout.setOnTouchListener((view, motionEvent) -> {
            int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                joystickView.setVisibility(View.VISIBLE);
                joystickView.setX(motionEvent.getX() - joystickView.getWidth() / 2f);
                joystickView.setY(motionEvent.getY() - joystickView.getHeight() / 2f);
            } else if (action == MotionEvent.ACTION_UP)
                joystickView.setVisibility(View.INVISIBLE);

            joystickView.onTouchEvent(motionEvent);
            return false;
        });

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
                mileageTextView.setText(String.format(Locale.UK, "%d km", estimatedMileage)));
        movementViewModel.getVelocity().observe(this, speedometerView::setVelocity);
        movementViewModel.getDistanceTravelled().observe(this, distanceTravelled ->
                traveledTextView.setText(String.format(Locale.UK, "%.2f km", distanceTravelled)));

        // TODO: Add check that client has selected Bluetooth and not TCP
        enableBluetooth();
    }

    private void enableBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Log.d(TAG, "Bluetooth is not supported on this device");
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            startBluetoothService();
            return;
        }

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    if (resultCode == Activity.RESULT_OK) {
                        Log.d(TAG, "Bluetooth enabled");
                        startBluetoothService();
                    } else if (resultCode == Activity.RESULT_CANCELED)
                        Log.d(TAG, "Bluetooth not enabled");
                });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        } else
            enableBtLauncher.launch(enableBtIntent);
    }

    private void startBluetoothService() {
        // bind view models to the service
        Intent startIntent = new Intent(this, BluetoothService.class);
        startService(startIntent);
        List<AbstractViewModel> viewModels = Arrays.asList(joystickViewModel, batteryViewModel, movementViewModel, warningViewModel);
        Intent bindIntent = new Intent(this, BluetoothService.class);
        viewModels.forEach(viewModel -> bindService(bindIntent, viewModel.getServiceConnection(), BIND_AUTO_CREATE));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Destroying activity");
        super.onDestroy();
    }
}