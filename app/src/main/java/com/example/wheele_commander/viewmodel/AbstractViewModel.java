package com.example.wheele_commander.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.wheele_commander.backend.INetworkClient;
import com.example.wheele_commander.backend.NetworkClient;

public abstract class AbstractViewModel extends AndroidViewModel {
    protected Context context;
    protected NetworkClient networkClient;
    protected ServiceConnection serviceConnection;

    public AbstractViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    public abstract void handleMessage(Message message);

    @Override
    protected void onCleared() {
        super.onCleared();
        context.unbindService(serviceConnection);
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }
}
