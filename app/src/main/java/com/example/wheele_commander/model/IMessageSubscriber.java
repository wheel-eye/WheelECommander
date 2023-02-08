package com.example.wheele_commander.model;

import android.os.Message;

public interface IMessageSubscriber {
    void handleMessage(Message msg);
}