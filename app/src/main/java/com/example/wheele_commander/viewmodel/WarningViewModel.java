package com.example.wheele_commander.viewmodel;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;

import androidx.lifecycle.ViewModel;

import com.example.wheele_commander.backend.INetworkClient;
import com.example.wheele_commander.backend.NetworkClient;
import com.example.wheele_commander.backend.IMessageSubscriber;

public class WarningViewModel extends ViewModel implements IMessageSubscriber {
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
            networkClient = binder.getService();
//            networkClient.subscribe(WarningViewModel.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCleared() {
        super.onCleared();
//        unbindService(serviceConnection);
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }
}
