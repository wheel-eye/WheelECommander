package com.example.wheele_commander.model;

import static com.example.wheele_commander.model.MessageType.JOYSTICK_MOVEMENT;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;

import backend_thread.NetworkClient;

public final class JoystickViewModel {
    private INetworkClient networkClient;

    public JoystickViewModel() {
    }

    public void onJoystickMove(int angle, int power) {
        Message msg = Message.obtain();
        msg.what = JOYSTICK_MOVEMENT.ordinal();
        msg.arg1 = angle;
        msg.arg2 = power;
        networkClient.sendMessage(msg);
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
