package com.example.wheele_commander.backend;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.wheele_commander.deserializer.Data;
import com.example.wheele_commander.deserializer.JsonDeserializer;
import com.example.wheele_commander.deserializer.Warning;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceiverThread extends Thread {
    private static final String TAG = "ReceiverThread";
    private final InputStream inputStream;
    private final Handler receiverHandler;
    private boolean running;

    public ReceiverThread(Handler receiverHandler, Socket socket) {
        this.receiverHandler = receiverHandler;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                String payload = readInputStream();
                receiverHandler.handleMessage(payloadToMessage(payload));
            } catch (IOException e) {
                Log.d(TAG, "run: Connection has been terminated");
                stopThread();
            }
        }
    }

    public void stopThread() {
        running = false;
    }

    private String readInputStream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        inputStream.read(buffer);
        outputStream.write(buffer);
        return outputStream.toString("UTF-8");
    }

    private Message payloadToMessage(String payload) {
        int messageType = Character.getNumericValue(payload.charAt(0));
        String json = payload.substring(1);

        Message msg = new Message();
        msg.what = messageType;

        if (messageType == 0)
            msg.obj = JsonDeserializer.getInstance().deserialize(json, Data.class);
        else if (messageType == 1)
            msg.obj = JsonDeserializer.getInstance().deserialize(json, Warning.class);

        return msg;
    }
}
