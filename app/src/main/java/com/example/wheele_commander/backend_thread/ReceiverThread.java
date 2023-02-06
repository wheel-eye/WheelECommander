package com.example.wheele_commander.backend_thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReceiverThread extends Thread{
    private Socket socket;
    private Handler receiverHandler;

    public ReceiverThread(Socket socket, Handler receiverHandler){
        this.socket = socket;
        this.receiverHandler = receiverHandler;
    }

    @Override
    public void run() {
        Looper.prepare();

        try {
            InputStream is = socket.getInputStream();
            byte[] data = readToByteArray(is);
            Message msg = new Message();
            msg.what = 1;
            msg.obj = data;
            receiverHandler.sendMessage(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Looper.loop();
    }

    private byte[] readToByteArray(InputStream stream) throws IOException {
        int nRead;
        byte[] data = new byte[4];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while ((nRead = stream.read(data, 0, data.length)) != 0) {
            baos.write(data, 0, nRead);
        }

        baos.flush();
        return baos.toByteArray();
    }
}
