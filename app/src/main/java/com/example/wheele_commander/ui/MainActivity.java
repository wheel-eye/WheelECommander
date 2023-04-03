package com.example.wheele_commander.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.wheele_commander.R;
import com.example.wheele_commander.backend.CommunicationService;
import com.example.wheele_commander.backend.ConnectionStatus;
import com.example.wheele_commander.backend.bluetooth.BluetoothService;
import com.example.wheele_commander.viewmodel.BatteryViewModel;
import com.example.wheele_commander.viewmodel.IViewModel;
import com.example.wheele_commander.viewmodel.JoystickViewModel;
import com.example.wheele_commander.viewmodel.MovementStatisticsViewModel;
import com.example.wheele_commander.viewmodel.WarningViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int RED_COLOR = Color.rgb(220, 50, 47);
    public static final int ORANGE_COLOR = Color.rgb(255, 153, 51);
    public static final int GREEN_COLOR = Color.rgb(34, 139, 34);
    public static final int PERMISSION_REQUEST_BLUETOOTH = 1;

    private JoystickViewModel joystickViewModel;
    private BatteryViewModel batteryViewModel;
    private MovementStatisticsViewModel movementViewModel;
    private WarningViewModel warningViewModel;
    private CommunicationService communicationService;

    private TextView statusTextView;
    private BatteryView batteryView;

    private boolean isBound = false;
    private ActivityResultLauncher<Intent> enableBtLauncher;
    private BluetoothAdapter bluetoothAdapter;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: Connected to service");
            CommunicationService.CommunicationServiceBinder binder = (CommunicationService.CommunicationServiceBinder) iBinder;
            communicationService = binder.getService();
            isBound = true;

            communicationService.getConnectionManager().getConnectionStatus().observe(MainActivity.this, s -> {
                if (s == ConnectionStatus.DISCONNECTED)
                    statusTextView.setTextColor(RED_COLOR);
                else if (s == ConnectionStatus.CONNECTING)
                    statusTextView.setTextColor(ORANGE_COLOR);
                else if (s == ConnectionStatus.CONNECTED)
                    statusTextView.setTextColor(GREEN_COLOR);
                statusTextView.setText(s.name());

                if (s == ConnectionStatus.DISCONNECTED || s == ConnectionStatus.CONNECTING) {
                    batteryViewModel.getBatteryCharge().postValue(0);
                    batteryViewModel.getEstimatedMileage().postValue(0f);
                    movementViewModel.getVelocity().postValue(0f);
                }
            });

            List<IViewModel> viewModels = Arrays.asList(batteryViewModel, joystickViewModel, movementViewModel, warningViewModel);
            viewModels.forEach(viewModel -> viewModel.registerCommunicationService(communicationService));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: Service disconnected");
            isBound = false;
        }
    };
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (!isBound && state == BluetoothAdapter.STATE_ON)
                    startBluetooth();
            }
        }
    };
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Creating MainActivity");

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        initViewModels();
        initView();

        enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    if (resultCode == Activity.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "Bluetooth enabled", Toast.LENGTH_LONG).show();
                        startBluetoothService();
                    } else if (resultCode == Activity.RESULT_CANCELED)
                        Toast.makeText(getApplicationContext(), "Bluetooth NOT enabled", Toast.LENGTH_LONG).show();
                });

        AlertDialog.Builder alertDialogBuilder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogStyle)
                .setTitle("Disable Brakes")
                .setMessage("Please confirm that you have disabled any brakes on your wheelchair. Not doing so could lead to motor damage.")
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                    enableBluetooth();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert);
        alertDialogBuilder.show();
        alertDialog = alertDialogBuilder
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> dialog.dismiss())
                .create();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        JoystickView joystickView = findViewById(R.id.joystickView);
        batteryView = findViewById(R.id.batteryView);
        SpeedometerView speedometerView = findViewById(R.id.speedometerView);

        statusTextView = findViewById(R.id.statusTextView);
        TextView mileageTextView = findViewById(R.id.mileageTextView);
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

        joystickView.setOnMoveListener((angle, strength) -> joystickViewModel.onJoystickMove(angle, strength));

        // observe view model variables and change views accordingly
        batteryViewModel.getBatteryCharge().observe(this, batteryLevel ->
                batteryView.setBatteryLevel(batteryLevel / 100f));
        batteryViewModel.getEstimatedMileage().observe(this, estimatedMileage ->
                mileageTextView.setText(String.format(Locale.UK, "%.2f km", estimatedMileage)));

        movementViewModel.getVelocity().observe(this, speedometerView::setVelocity);
        movementViewModel.getDistanceTravelled().observe(this, distanceTravelled -> {
            batteryViewModel.setDistanceTravelled(distanceTravelled);
            traveledTextView.setText(String.format(Locale.UK, "%.2f km", distanceTravelled));
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        alertDialog.show();
    }

    private void initViewModels() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        joystickViewModel = viewModelProvider.get(JoystickViewModel.class);
        batteryViewModel = viewModelProvider.get(BatteryViewModel.class);
        movementViewModel = viewModelProvider.get(MovementStatisticsViewModel.class);
        warningViewModel = viewModelProvider.get(WarningViewModel.class);
    }

    private void enableBluetooth() {
        Log.d(TAG, "Enabling Bluetooth");

        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
            finishAffinity();
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
            if (permission == PackageManager.PERMISSION_GRANTED)
                startBluetooth();
            else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_BLUETOOTH);
        } else
            startBluetooth();
    }

    private void startBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is already enabled");
            startBluetoothService();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
        }
    }

    private void startBluetoothService() {
        Intent startIntent = new Intent(this, BluetoothService.class);
        startService(startIntent);
        Intent bindIntent = new Intent(this, BluetoothService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show();
                startBluetooth();
            } else
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Destroying activity");
        if (isBound) {
            unregisterReceiver(receiver);
            unbindService(serviceConnection);
        }
        isBound = false;
    }
}
