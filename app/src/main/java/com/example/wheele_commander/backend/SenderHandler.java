package com.example.wheele_commander.backend;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SenderHandler extends Handler {
    private static final String TAG = "SenderHandler";
    private static final int BUFFER_SIZE = 8;
    private final ByteBuffer byteBuffer;
    private final DataOutputStream outputStream;

    public SenderHandler(Looper looper, Socket socket) {
        super(looper);
        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        byteBuffer.clear();
        byteBuffer.putInt(msg.arg1);
        byteBuffer.putInt(msg.arg2);
        try {
            outputStream.write(byteBuffer.array());
            outputStream.flush();
        } catch (IOException e) {
            Log.d(TAG, "Error writing to output stream: " + e.getMessage());
        }
    }
}
