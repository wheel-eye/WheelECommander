package com.example.wheele_commander.backend.network;

import android.os.SystemClock;
import android.util.Log;

import com.example.wheele_commander.backend.interfaces.AbstractConnectionManager;
import com.example.wheele_commander.backend.interfaces.IConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkConnectionManager extends AbstractConnectionManager {
    private static final String HARDWARE_IP = "172.20.118.23"; // or 100.90.35.131
    private static final int HARDWARE_PORT_NUMBER = 5000;
    private static final int SOCKET_CONNECTION_TIMEOUT = 2000;

    private Socket socket;

    public NetworkConnectionManager() {
        TAG = "NetworkConnectionManage";
    }

    @Override
    protected void onConnectionLost() {
        Log.d(TAG, "Network connection lost");
        super.onConnectionLost();
    }

    @Override
    public void createChannel() {
        socket = new Socket();
    }

    @Override
    public IConnection connectChannel() {
        while (true) {
            try {
                socket.connect(new InetSocketAddress(HARDWARE_IP, HARDWARE_PORT_NUMBER), SOCKET_CONNECTION_TIMEOUT);
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
}
