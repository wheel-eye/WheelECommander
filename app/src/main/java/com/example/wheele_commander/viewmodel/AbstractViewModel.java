package com.example.wheele_commander.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;

import com.example.wheele_commander.backend.NetworkClient;
import com.example.wheele_commander.model.Wheelchair;

public abstract class AbstractViewModel extends AndroidViewModel {
    @SuppressLint("StaticFieldLeak")
    protected Context context;
    @SuppressLint("StaticFieldLeak")
    protected NetworkClient networkClient;
    protected ServiceConnection serviceConnection;
    protected Observer<Message> messageObserver;

    protected Wheelchair wheelchair;

    public AbstractViewModel(@NonNull Application application, @NonNull Wheelchair wheelchair) {
        super(application);
        context = application.getApplicationContext();
        messageObserver = this::handleMessage;

        this.wheelchair = wheelchair;
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
