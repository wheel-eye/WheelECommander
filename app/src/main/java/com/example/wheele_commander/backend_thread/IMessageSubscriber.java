package com.example.wheele_commander.backend_thread;

import android.os.Message;

public interface IMessageSubscriber {
    void handleMessage(Message message);
}
