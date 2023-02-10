package com.example.wheele_commander.viewmodel;

import static com.example.wheele_commander.viewmodel.MessageType.JOYSTICK_MOVEMENT;

import com.example.wheele_commander.backend.INetworkClient;
import com.example.wheele_commander.backend.NetworkClient;

import android.content.ComponentName;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.os.Message;

import android.util.Log;

import androidx.lifecycle.ViewModel;

/**
 * relays joystick user input to {@link NetworkClient} such that it may be sent to the connected hardware.
 *
 * Sends {@code JOYSTICK_MOVEMENT} messages.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see com.example.wheele_commander.viewmodel.MessageType
 */
public class JoystickViewModel extends ViewModel {
    private static final String TAG = "JoystickViewModel";
    private static INetworkClient networkClient;

    public JoystickViewModel() {
    }

    /**
     * relays joystick user input to {@link NetworkClient} such that it may be sent to the connected hardware.
     *
     * @param angle see (link joystick source)
     * @param power see (link joystick source)
     */
    public void onJoystickMove(int angle, int power) {
        if (networkClient != null) {
            Message msg = Message.obtain();
            msg.what = JOYSTICK_MOVEMENT.ordinal();
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

