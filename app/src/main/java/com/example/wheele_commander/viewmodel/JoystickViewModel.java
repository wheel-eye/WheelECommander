package com.example.wheele_commander.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wheele_commander.backend.NetworkClient;

public class JoystickViewModel extends AbstractViewModel {
    private static final String TAG = "JoystickViewModel";

    public JoystickViewModel(@NonNull Application application) {
        super(application);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected to service");
                NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
                networkClient = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: Service disconnected");
            }
        };
    }

    public void onJoystickMove(int angle, int power) {
        if (networkClient != null) {
            Message msg = Message.obtain();
            msg.what = MessageType.JOYSTICK_MOVEMENT.ordinal();

            if (angle > 180)
                angle -= 360;

            msg.arg1 = angle;
            msg.arg2 = power;
            networkClient.sendMessage(msg);
        }
    }

    @Override
    public void handleMessage(Message message) {
    }
}

