package com.example.wheele_commander.backend.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.backend.ConnectionStatus;
import com.example.wheele_commander.backend.interfaces.IConnection;
import com.example.wheele_commander.backend.listeners.IConnectionListener;
import com.example.wheele_commander.backend.listeners.IConnectionReconnectListener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BluetoothConnectionManager {
    private static final String TAG = "BluetoothConnectionManager";
    private static final long RECONNECT_DELAY_MS = 2000L;
    private static final int SERVER_PORT = 5;

    private final IConnectionListener connectionListener;
    private final MutableLiveData<ConnectionStatus> connectionStatus;
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothDevice device;

    private boolean stopReconnect;
    private IConnectionReconnectListener reconnectListener;
    private Method createRfcommSocket;
    private BluetoothSocket socket;

    public BluetoothConnectionManager(BluetoothAdapter bluetoothAdapter, BluetoothDevice device) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.device = device;

        connectionStatus = new MutableLiveData<>(ConnectionStatus.DISCONNECTED);
        connectionListener = this::onConnectionLost;

        try {
            createRfcommSocket = device.getClass().getMethod("createRfcommSocket", int.class);
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "createRfcommSocket method not found");
        }
    }

    private void onConnectionLost() {
        Log.d(TAG, "Bluetooth connection lost");
        socket = null;
        connectionStatus.postValue(ConnectionStatus.DISCONNECTED);
        // reconnect in new Thread since it is a blocking operation
        new Thread(this::connect).start();
    }

    private IConnection createConnection() throws IOException {
        try {
            socket = (BluetoothSocket) createRfcommSocket.invoke(device, SERVER_PORT);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.d(TAG, "Failed to create socket", e);
            return null;
        }
        if (socket == null)
            return null;

        socket.connect();
        IConnection connection = new BluetoothConnection(socket);
        connection.setConnectionListener(connectionListener);
        return connection;
    }

    public void connect() {
        stopReconnect = false;
        while (!stopReconnect) {
            if (!bluetoothAdapter.isEnabled()) {
                if (connectionStatus.getValue() == ConnectionStatus.CONNECTING)
                    connectionStatus.postValue(ConnectionStatus.DISCONNECTED);
                continue;
            }
            if (connectionStatus.getValue() == ConnectionStatus.DISCONNECTED)
                connectionStatus.postValue(ConnectionStatus.CONNECTING);

            try {
                IConnection newConnection = null;
                if (socket == null || !socket.isConnected()) {
                    connectionStatus.postValue(ConnectionStatus.CONNECTING);
                    newConnection = createConnection();
                }
                reconnectListener.onReconnect(newConnection);
                connectionStatus.postValue(ConnectionStatus.CONNECTED);
                Log.d(TAG, "Connected via Bluetooth to " + device.getName());
                return;
            } catch (IOException e) {
                Log.d(TAG, "Couldn't connect to device, retrying in 2 sec...");
            }

            SystemClock.sleep(RECONNECT_DELAY_MS);
        }
    }

    public void disconnect() {
        stopReconnect = true;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.d(TAG, "Could not close the client socket");
            }
        }
        connectionStatus.postValue(ConnectionStatus.DISCONNECTED);
    }

    public void setReconnectListener(IConnectionReconnectListener reconnectListener) {
        this.reconnectListener = reconnectListener;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public LiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }
}
