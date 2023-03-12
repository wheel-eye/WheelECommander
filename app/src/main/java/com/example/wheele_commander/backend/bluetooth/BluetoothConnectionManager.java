package com.example.wheele_commander.backend.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;

import com.example.wheele_commander.backend.interfaces.IConnection;
import com.example.wheele_commander.backend.interfaces.IConnectionManager;
import com.example.wheele_commander.backend.listeners.IConnectionListener;
import com.example.wheele_commander.backend.listeners.IConnectionReconnectListener;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectionManager implements IConnectionManager {
    private static final String TAG = "BluetoothConnectionManager";
    private static final UUID DEVICE_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private static final long RECONNECT_DELAY_MS = 2000L;

    private final IConnectionListener connectionListener = this::onConnectionLost;
    private final BluetoothDevice device;

    private IConnectionReconnectListener reconnectListener;
    private BluetoothSocket socket;
    private IConnection connection;

    public BluetoothConnectionManager(BluetoothDevice device) {
        this.device = device;
    }

    private void onConnectionLost() {
        Log.d(TAG, "Bluetooth connection lost");
        // reconnect in new Thread since it is a blocking operation
        new Thread(BluetoothConnectionManager.this::attemptToReconnect).start();
    }

    private void attemptToReconnect() {
        while (true) {
            createChannel();
            IConnection newConnection = connectChannel();
            if (newConnection != null) {
                reconnectListener.onReconnect(newConnection);
                connection = newConnection;
                connection.setConnectionListener(connectionListener);
                Log.d(TAG, "Reconnection successful");
                return;
            }

            Log.d(TAG, "Failed to connect, retrying in 2 sec...");
            SystemClock.sleep(RECONNECT_DELAY_MS);
        }
    }

    @Override
    public void createChannel() {
        BluetoothSocket tmpSocket = null;
        try {
            tmpSocket = device.createRfcommSocketToServiceRecord(DEVICE_UUID);
        } catch (IOException e) {
            Log.d(TAG, "Failed to create socket", e);
        }
        socket = tmpSocket;
    }

    @Override
    public IConnection connectChannel() {
        while (true) {
            try {
                socket.connect();
                Log.d(TAG, "Connected via Bluetooth to " + device.getName());
                connection = new BluetoothConnection(socket);
                connection.setConnectionListener(connectionListener);
                return connection;
            } catch (IOException e) {
                Log.d(TAG, "Couldn't connect to device, retrying in 2 sec...");
                SystemClock.sleep(RECONNECT_DELAY_MS);
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.d(TAG, "Could not close the client socket");
        }
    }

    @Override
    public void setReconnectListener(IConnectionReconnectListener reconnectListener) {
        this.reconnectListener = reconnectListener;
    }
}
