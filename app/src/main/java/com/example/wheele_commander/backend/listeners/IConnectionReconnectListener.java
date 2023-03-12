package com.example.wheele_commander.backend.listeners;

import com.example.wheele_commander.backend.interfaces.IConnection;

public interface IConnectionReconnectListener {
    void onReconnect(IConnection connection);
}
