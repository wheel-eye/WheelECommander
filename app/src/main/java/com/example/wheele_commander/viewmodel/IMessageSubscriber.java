package com.example.wheele_commander.viewmodel;

import android.os.Message;

public interface IMessageSubscriber {
    void handleMessage(Message msg);
}