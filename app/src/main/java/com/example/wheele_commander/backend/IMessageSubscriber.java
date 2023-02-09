package com.example.wheele_commander.backend;

import android.os.Message;

public interface IMessageSubscriber {
    void handleMessage(Message message);
}
