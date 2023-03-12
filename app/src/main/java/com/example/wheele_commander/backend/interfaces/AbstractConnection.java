package com.example.wheele_commander.backend.interfaces;

import com.example.wheele_commander.backend.listeners.IConnectionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractConnection implements IConnection {
    protected String TAG;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected IConnectionListener connectionListener;

    public void send(byte[] data) {
        try {
            outputStream.write(data);
        } catch (IOException e) {
            connectionListener.onConnectionLost();
        }
    }

    public byte[] receive(int desiredNumBytes) {
        byte[] buffer = new byte[desiredNumBytes];
        int bytesRead;
        try {
            bytesRead = inputStream.read(buffer);
        } catch (IOException e) {
            connectionListener.onConnectionLost();
            return null;
        }
        byte[] data = new byte[bytesRead];
        System.arraycopy(buffer, 0, data, 0, bytesRead);
        return data;
    }

    public void setConnectionListener(IConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }
}
