package com.example.wheele_commander.backend;

import android.os.Message;
import android.util.Log;

import com.example.wheele_commander.backend.interfaces.ICommunicationThread;
import com.example.wheele_commander.backend.interfaces.IConnection;
import com.example.wheele_commander.backend.listeners.IReceiveListener;
import com.example.wheele_commander.deserializer.Data;
import com.example.wheele_commander.deserializer.JsonDeserializer;
import com.example.wheele_commander.deserializer.Warning;

import java.nio.ByteBuffer;

public class CommunicationThread extends Thread implements ICommunicationThread {
    private static final String TAG = "CommunicationThread";
    private static final int BUFFER_SIZE = 12;

    private final IConnection connection;
    private final ByteBuffer byteBuffer;

    private IReceiveListener receiveListener;
    private volatile boolean stopped;

    public CommunicationThread(IConnection connection) {
        this.connection = connection;
        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        stopped = false;
    }

    @Override
    public void run() {
        Log.d(TAG, "Communication thread started");

        while (!stopped) {
            byte[] headerBytes = connection.receive(Header.HEADER_SIZE);
            if (headerBytes == null) {
                stopped = true;
                Log.d(TAG, "Connection error, stopping communication thread");
                return;
            }

            Header header = Header.fromBytes(headerBytes);
            Message msg = Message.obtain();
            msg.what = header.getMessageType();
            byte[] dataBytes = connection.receive(header.getDataLength());

            switch (header.getMessageType()) {
                case MessageConstants.DATA_MESSAGE:
                    Data data = JsonDeserializer.getInstance().deserialize(dataBytes, Data.class);
                    if (data == null)
                        continue;
                    msg.obj = data;
                    break;
                case MessageConstants.WARNING_MESSAGE:
                    msg.obj = JsonDeserializer.getInstance().deserialize(dataBytes, Warning.class);
                    break;
            }

            receiveListener.onMessageReceived(msg);
        }
    }

    public void stopCommunication() {
        stopped = true;
    }

    @Override
    public void send(Message message) {
        // TODO: Refactor this mess
        byteBuffer.clear();
        int dataLengthBytes = 8;
        byte[] headerBytes = {0x00, 0x00, 0x00, (byte) dataLengthBytes};
        byteBuffer.put(headerBytes);
        byteBuffer.putInt(message.arg1);
        byteBuffer.putInt(message.arg2);
        connection.send(byteBuffer.array());
    }

    @Override
    public void setReceiveListener(IReceiveListener receiveListener) {
        this.receiveListener = receiveListener;
    }
}
