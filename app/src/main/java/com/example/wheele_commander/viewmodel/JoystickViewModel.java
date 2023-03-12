package com.example.wheele_commander.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wheele_commander.backend.CommunicationService;

/**
 * relays joystick user input to {@link com.example.wheele_commander.backend.CommunicationService} such that it may be sent to the connected hardware.
 * <p>
 * Sends {@code JOYSTICK_MOVEMENT} messages.
 *
 * @author Konrad Pawlikowski
 * @author Peter Marks
 * @see com.example.wheele_commander.viewmodel.MessageType
 */
public class JoystickViewModel extends AbstractViewModel {
    private static final String TAG = "JoystickViewModel";

    public JoystickViewModel(@NonNull Application application) {
        super(application);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected to service");
                CommunicationService.CommunicationServiceBinder binder = (CommunicationService.CommunicationServiceBinder) iBinder;
                communicationService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: Service disconnected");
            }
        };
    }

    /**
     * relays joystick user input to {@link CommunicationService} such that it may be sent to the connected hardware.
     *
     * @param angle see (link joystick source)
     * @param power see (link joystick source)
     */
    public void onJoystickMove(int angle, int power) {
        if (communicationService != null) {
            Message message = Message.obtain();
            message.what = MessageType.JOYSTICK_MOVEMENT;

            angle -= 90;
            if (angle > 180)
                angle -= 360;

            message.arg1 = angle;
            message.arg2 = power;
            communicationService.send(message);
        }
    }

    @Override
    public void handleMessage(Message message) {
    }
}
