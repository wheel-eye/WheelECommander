package com.example.wheele_commander.backend.interfaces;

import android.os.SystemClock;
import android.util.Log;

import com.example.wheele_commander.backend.listeners.IConnectionListener;
import com.example.wheele_commander.backend.listeners.IConnectionReconnectListener;

public abstract class AbstractConnectionManager {
    protected static final long RECONNECT_DELAY_MS = 2000L;

    protected final IConnectionListener connectionListener = this::onConnectionLost;
    protected String TAG = "AbstractConnectionManager";
    protected IConnection connection;
    private IConnectionReconnectListener reconnectListener;

    public abstract void createChannel();

    public abstract IConnection connectChannel();

    public abstract void disconnect();

    protected void onConnectionLost() {
        connection = null;
        // reconnect in new Thread since it is a blocking operation
        new Thread(this::attemptToReconnect).start();
    }

    protected void attemptToReconnect() {
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

    public void setReconnectListener(IConnectionReconnectListener reconnectListener) {
        this.reconnectListener = reconnectListener;
    }

    public boolean isConnected() {
        return connection != null;
    }
}
