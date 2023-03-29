package com.example.wheele_commander.backend.interfaces;

import android.os.SystemClock;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.backend.ConnectionStatus;
import com.example.wheele_commander.backend.listeners.IConnectionListener;
import com.example.wheele_commander.backend.listeners.IConnectionReconnectListener;

public abstract class AbstractConnectionManager {
    protected static final long RECONNECT_DELAY_MS = 2000L;

    protected final IConnectionListener connectionListener = this::onConnectionLost;
    protected String TAG = "AbstractConnectionManager";
    protected IConnection connection;
    protected boolean stopReconnect;
    protected MutableLiveData<ConnectionStatus> connectionStatus;
    private IConnectionReconnectListener reconnectListener;

    protected AbstractConnectionManager() {
        connectionStatus = new MutableLiveData<>(ConnectionStatus.DISCONNECTED);
    }

    public abstract void createChannel();

    public abstract IConnection connectChannel();

    public void disconnect() {
        stopReconnect = true;
        connectionStatus.postValue(ConnectionStatus.DISCONNECTED);
    }

    protected void onConnectionLost() {
        connection = null;
        // reconnect in new Thread since it is a blocking operation
        new Thread(this::attemptToReconnect).start();
    }

    protected void attemptToReconnect() {
        stopReconnect = false;
        createChannel();
        while (!stopReconnect) {
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

    public MutableLiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }
}
