package com.example.wheele_commander.viewmodel;

import android.os.Message;

import androidx.lifecycle.ViewModel;

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
public class JoystickViewModel extends ViewModel implements IViewModel {
    private static final String TAG = "JoystickViewModel";
    private CommunicationService communicationService;

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
    public void registerCommunicationService(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }
}
