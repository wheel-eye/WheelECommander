package com.example.wheele_commander.backend.network;

import android.os.SystemClock;
import android.util.Log;

import com.example.wheele_commander.backend.interfaces.IConnection;
import com.example.wheele_commander.backend.interfaces.IConnectionManager;
import com.example.wheele_commander.backend.listeners.IConnectionListener;
import com.example.wheele_commander.backend.listeners.IConnectionReconnectListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkConnectionManager implements IConnectionManager {
    private static final String TAG = "NetworkConnectionManage";
    private static final String HARDWARE_IP = "172.20.118.23"; // or 100.90.35.131
    private static final int HARDWARE_PORT_NUMBER = 5000;
    private static final long RECONNECT_DELAY_MS = 2000L;
    private static final int CONNECTION_TIMEOUT = 2000;

    private final IConnectionListener connectionListener = this::onConnectionLost;

    private IConnectionReconnectListener reconnectListener;
    private Socket socket;
    private IConnection connection;

    private void onConnectionLost() {
        Log.d(TAG, "Network connection lost");
        // reconnect in new Thread since it is a blocking operation
        new Thread(NetworkConnectionManager.this::attemptToReconnect).start();
    }

    private void attemptToReconnect() {
        createChannel();
        while (true) {
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
        socket = new Socket();
    }

    @Override
    public IConnection connectChannel() {
        while (true) {
            try {
                socket.connect(new InetSocketAddress(HARDWARE_IP, HARDWARE_PORT_NUMBER), CONNECTION_TIMEOUT);
                socket.setKeepAlive(true);
                connection = new NetworkConnection(socket);
                connection.setConnectionListener(connectionListener);
                Log.d(TAG, "Connected to " + HARDWARE_IP + ":" + HARDWARE_PORT_NUMBER);
                return connection;
            } catch (IOException e) {
                Log.d(TAG, "Connection failed, reconnecting in 2 sec...");
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
