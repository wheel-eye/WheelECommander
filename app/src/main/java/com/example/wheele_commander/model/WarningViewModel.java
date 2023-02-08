package com.example.wheele_commander.model;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;

import backend_thread.NetworkClient;

public final class WarningViewModel implements IMessageSubscriber {
    private INetworkClient networkClient;

    public WarningViewModel() {
    }

    @Override
    public void handleMessage(Message msg) {
        // handle warning message
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
            networkClient = binder.getService(binder);
            networkClient.subscribe(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }
}
