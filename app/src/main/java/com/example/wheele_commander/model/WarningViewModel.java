package com.example.wheele_commander.model;

import android.os.Message;

import backend_thread.NetworkClient;
public final class WarningViewModel implements IMessageSubscriber{

    WarningViewModel(){
        NetworkClient.subscribe(this); // how do I know the Network client?
    }

    @Override
    public void handleMessage(Message msg) {
        // handle warning message
    }
}
