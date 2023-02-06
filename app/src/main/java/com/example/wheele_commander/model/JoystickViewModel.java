package com.example.wheele_commander.model;

import static com.example.wheele_commander.model.MessageType.JOYSTICK_MOVEMENT;

import android.os.Message;

import backend_thread.NetworkClient;

public final class JoystickViewModel implements IMessageSubscriber{

    JoystickViewModel(){
        NetworkClient.subscribe(this); // how do I know the Network client?
    }

    @Override
    public void handleMessage(Message msg) {
        throw new IllegalArgumentException("JoystickViewModel does not handle any messages");
    }

    public void onJoystickMove(int angle, int power){
        Message msg = Message.obtain();
        msg.what = JOYSTICK_MOVEMENT.ordinal();
        msg.arg1 = angle;
        msg.arg2 = power;
        NetworkClient.sendMessage(msg);
    }

}
