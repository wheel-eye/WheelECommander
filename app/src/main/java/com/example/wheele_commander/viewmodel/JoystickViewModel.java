package com.example.wheele_commander.viewmodel;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.wheele_commander.backend.INetworkClient;
import com.example.wheele_commander.backend.NetworkClient;

public class JoystickViewModel extends ViewModel {
    private static final String TAG = "JoystickViewModel";
    static INetworkClient networkClient;

    public JoystickViewModel() {
    }

    public void onJoystickMove(int angle, int power) {
        if (networkClient != null) {
            Message msg = Message.obtain();
            msg.what = MessageType.JOYSTICK_MOVEMENT.ordinal();
            msg.arg1 = angle;
            msg.arg2 = power;
            networkClient.sendMessage(msg);
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: Connected to service");
            NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
            JoystickViewModel.networkClient = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: Service disconnected");
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

