package com.example.wheele_commander.backend.interfaces;

import android.os.Message;

import com.example.wheele_commander.backend.listeners.IReceiveListener;

public interface ICommunicationThread {
    void send(Message message);

    void setReceiveListener(IReceiveListener receiveListener);
}
