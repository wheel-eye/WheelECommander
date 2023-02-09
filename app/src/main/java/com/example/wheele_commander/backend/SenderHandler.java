package com.example.wheele_commander.backend;

import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import android.os.Handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SenderHandler extends Handler {
    private final Socket socket;
    private final DataOutputStream outputStream;

    public SenderHandler(Looper looper, Socket socket) {
        super(looper);

        this.socket = socket;
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case 1:
                // Handle message 1
                break;
            case 2:
                // Handle message 2
                break;
            default:
                super.handleMessage(msg);
        }
    }

    private void sendThroughSocket(byte[] data) throws IOException {
        outputStream.write(data);
        outputStream.flush();
    }
}
