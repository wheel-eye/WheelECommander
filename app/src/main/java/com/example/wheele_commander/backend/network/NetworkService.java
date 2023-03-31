package com.example.wheele_commander.backend.network;

import android.util.Log;

import com.example.wheele_commander.backend.CommunicationService;

public class NetworkService extends CommunicationService {
    protected String TAG = "NetworkService";

    @Override
    protected void initializeService() {
        Log.d(TAG, "onCreate: Network Service client created");

//        connectionManager = new NetworkConnectionManager();
        connectionManager.setReconnectListener(reconnectListener);
        // essential as Android doesn't allow socket set-up in main thread -> NetworkOnMainThreadException
        new Thread(connectionManager::connect).start();
    }
}
