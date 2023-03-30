package com.example.wheele_commander.backend.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

import com.example.wheele_commander.backend.CommunicationService;
import com.example.wheele_commander.backend.interfaces.IConnection;

public class BluetoothService extends CommunicationService {
    private static final String MAC_ADDRESS = "DC:A6:32:18:06:59";
//    private static final String MAC_ADDRESS = "90:61:AE:3F:DE:B3";

    protected String TAG = "BluetoothService";

    @SuppressLint("MissingPermission")
    @Override
    protected void initializeService() {
        Log.d(TAG, "onCreate: Bluetooth Client Service created");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // I don't know how else to access it
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC_ADDRESS);

        Log.d(TAG, "initializeService: " + bluetoothDevice);
        if (bluetoothDevice == null) {
            Toast.makeText(getApplicationContext(), "Device not reachable", Toast.LENGTH_LONG).show();
            return;
        }

        connectionManager = new BluetoothConnectionManager(bluetoothAdapter, bluetoothDevice);
        connectionManager.createChannel();
        connectionManager.setReconnectListener(reconnectListener);
        new Thread(() -> {
            IConnection connection = connectionManager.connectChannel();
            if (connection != null)
                startCommunicationThread(connection);
        }).start();
    }
}
