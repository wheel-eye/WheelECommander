package com.example.wheele_commander.backend.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;

import com.example.wheele_commander.backend.interfaces.AbstractConnectionManager;
import com.example.wheele_commander.backend.interfaces.IConnection;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothConnectionManager extends AbstractConnectionManager {
    private static final UUID DEVICE_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private static final int SERVER_PORT = 1;

    private final BluetoothDevice device;
    private BluetoothSocket socket;

    public BluetoothConnectionManager(BluetoothDevice device) {
        TAG = "BluetoothConnectionManager";
        this.device = device;
    }

    @Override
    protected void onConnectionLost() {
        Log.d(TAG, "Bluetooth connection lost");
        super.onConnectionLost();
    }

    @Override
    public void createChannel() {
        BluetoothSocket tmpSocket = null;
        try {
            tmpSocket = device.createRfcommSocketToServiceRecord(DEVICE_UUID);
            Method createRfcommSocket = device.getClass().getMethod("createRfcommSocket", int.class);
            tmpSocket = (BluetoothSocket) createRfcommSocket.invoke(device, SERVER_PORT);
        } catch (IOException e) {
            Log.d(TAG, "Failed to create socket", e);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
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
}
