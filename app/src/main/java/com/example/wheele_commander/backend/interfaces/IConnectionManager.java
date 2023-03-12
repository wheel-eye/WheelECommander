package com.example.wheele_commander.backend.interfaces;

import com.example.wheele_commander.backend.listeners.IConnectionReconnectListener;

public interface IConnectionManager {
    void createChannel();

    IConnection connectChannel();

    void disconnect();

    void setReconnectListener(IConnectionReconnectListener reconnectListener);
}
