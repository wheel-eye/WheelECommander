package com.example.wheele_commander.backend.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.example.wheele_commander.backend.CommunicationService;
import com.example.wheele_commander.backend.interfaces.IConnection;

public class BluetoothService extends CommunicationService {
    private static final String MAC_ADDRESS = "B8:27:EB:C4:80:A1";
    protected String TAG = "BluetoothService";

    @Override
    protected void initializeService() {
        Log.d(TAG, "onCreate: Bluetooth Client Service created");

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // I don't know how else to access it
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC_ADDRESS);
        if (bluetoothDevice == null) {
            Log.d(TAG, "Bluetooth device not reachable");
            return;
        }

        connectionManager = new BluetoothConnectionManager(bluetoothDevice);
        connectionManager.createChannel();
        connectionManager.setReconnectListener(reconnectListener);
        new Thread(() -> {
            IConnection connection = connectionManager.connectChannel();
            startCommunicationThread(connection);
        }).start();
    }
}
