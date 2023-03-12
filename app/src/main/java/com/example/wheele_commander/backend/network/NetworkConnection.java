package com.example.wheele_commander.backend.network;

import android.util.Log;

import com.example.wheele_commander.backend.interfaces.AbstractConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NetworkConnection extends AbstractConnection {
    public NetworkConnection(Socket socket) {
        TAG = "NetworkConnection";

        InputStream tmpInputStream = null;
        try {
            tmpInputStream = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        inputStream = tmpInputStream;

        OutputStream tmpOutputStream = null;
        try {
            tmpOutputStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        outputStream = tmpOutputStream;
    }
}