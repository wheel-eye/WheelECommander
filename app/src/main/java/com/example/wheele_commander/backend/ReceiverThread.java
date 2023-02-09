package com.example.wheele_commander.backend;

import static com.example.wheele_commander.viewmodel.MessageType.*;

import android.os.Handler;
import android.os.Message;

import com.example.wheele_commander.deserializer.Deserializer;
import com.example.wheele_commander.viewmodel.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

public class ReceiverThread extends Thread {
    private final Socket socket;
    private final Handler receiverHandler;
    private boolean running;

    public ReceiverThread(Handler receiverHandler, Socket socket) {
        this.receiverHandler = receiverHandler;
        this.socket = socket;
        running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] inputBytes = readToByteArray(inputStream);
                receiverHandler.handleMessage(deserializeToMessage(inputBytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopThread() {
        running = false;
    }

    private byte[] readToByteArray(InputStream stream) throws IOException {
        int nRead;
        byte[] data = new byte[4];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((nRead = stream.read(data, 0, data.length)) != 0) {
            outputStream.write(data, 0, nRead);
        }

        outputStream.flush();
        return outputStream.toByteArray();
    }

    private Message deserializeToMessage(byte[] stream) {
        byte[] data = Arrays.copyOfRange(stream, 1, stream.length);
        Message msg = new Message();
        if (Byte.toUnsignedInt(stream[0]) == 0) {
            msg.what = 0;
            msg.obj = Deserializer.getWarning(data);
        } else {
            msg.what = 1;
            msg.obj = Deserializer.getData(data);
        }
        return msg;
    }
}
