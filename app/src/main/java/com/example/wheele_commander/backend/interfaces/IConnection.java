package com.example.wheele_commander.backend.interfaces;

import com.example.wheele_commander.backend.listeners.IConnectionListener;

public interface IConnection {
    void send(byte[] data);

    byte[] receive(int desiredNumBytes);

    void setConnectionListener(IConnectionListener connectionListener);
}
